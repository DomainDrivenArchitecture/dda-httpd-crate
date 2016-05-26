; Licensed to the Apache Software Foundation (ASF) under one
; or more contributor license agreements. See the NOTICE file
; distributed with this work for additional information
; regarding copyright ownership. The ASF licenses this file
; to you under the Apache License, Version 2.0 (the
; "License"); you may not use this file except in compliance
; with the License. You may obtain a copy of the License at
;
; http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(ns org.domaindrivenarchitecture.pallet.crate.httpd
  (:require
    [schema.core :as s]
    [schema-tools.core :as st]
    [org.domaindrivenarchitecture.config.commons.map-utils :as map-utils]
    [httpd.crate.apache2 :as apache2]
    [httpd.crate.vhost :as vhost]
    [httpd.crate.config :as httpd-config]
    [httpd.crate.basic-auth :as auth]
    [httpd.crate.mod-gnutls :as gnutls]
    [httpd.crate.mod-jk :as jk]
    [httpd.crate.google-siteownership-verification :as google]
    [httpd.crate.common :as httpd-common]
    [httpd.crate.mod-rewrite :as rewrite]
    [httpd.crate.webserver-maintainance :as maintainance]
    [org.domaindrivenarchitecture.pallet.crate.liferay.web :as web]
  ))


(def HttpdConfig
  "defines a schema for a httpdConfig"
  {(s/optional-key :httpd)
   (s/conditional
       #(= (:letsencrypt %) true)
       {:letsencrypt (s/eq true) 
        :letsencrypt-mail s/Str
        :fqdn s/Str
        (s/optional-key :app-port) s/Str
        (s/optional-key :google-id) s/Str
        (s/optional-key :maintainance-page-content) [s/Str]}
       #(= (:letsencrypt %) false)
       {:letsencrypt (s/eq false) 
        :domain-cert s/Str 
        :domain-key s/Str 
        (s/optional-key :ca-cert) s/Str
        :fqdn s/Str
        (s/optional-key :app-port) s/Str
        (s/optional-key :google-id) s/Str
        (s/optional-key :maintainance-page-content) [s/Str]})
   })


;TODO: verify types
(def VhostConfig
  "defindes a schema for a VhostConfig"
  {:listening-port s/Str 
   :ca-cert s/Any
   :domain-name s/Any
   :domain-cert s/Any
   :domain-key s/Any
   :server-admin-email s/Str
   :google-id s/Str
   :consider-jk s/Bool
   :letsencrypt s/Bool
   :maintainance-page-content [s/Str]
   })



(s/defn vhost-head-wrapper
  "wrapper function for the vhost-head function in the httpd-crate"
  [config :- VhostConfig]
  (vhost/vhost-head (st/get-in config [:listening-port :domain-name :server-admin-email])))

(def default-httpd-webserver-configuration
  {:httpd {; Webserver Configuration
           :letsencrypt true
           :fqdn "localhost.localdomain"
           :app-port "8009"
           :maintainance-page-content ["<h1>Webserver Maintainance Mode</h1>"]}})

(s/defn ^:always-validate merge-config :- HttpdConfig
  "merges the partial config with default config & ensures that resulting config is valid."
  [partial-config]
  (map-utils/deep-merge default-httpd-webserver-configuration partial-config))

(def vhost-tail-wrapper ["</VirtualHost>"])

(s/defn prefix-wrapper
  [config :- VhostConfig]
  (httpd-common/prefix
    "  " 
    (into 
      []
      (concat
        ["Alias /quiz/ \"/var/www/static/quiz/\""
         ""]
        (jk/vhost-jk-mount :path "/*")
        (jk/vhost-jk-unmount :path "/quiz/*")
        [""]
        (google/vhost-ownership-verification 
          (st/get-in config :google-id)
          (st/get-in config :consider-jk)
        (maintainance/vhost-service-unavailable-error-page
          (st/get-in config :consider-jk))
        (vhost/vhost-log 
          :error-name "error.log"
          :log-name "ssl-access.log"
          :log-format "combined")
        (if (st/get-in config :letsencrypt)
          (gnutls/vhost-gnutls-letsencrypt (st/get-in config :domain-name))
          (gnutls/vhost-gnutls (st/get-in config :domain-name)))
        )))))

(defn install-webserver
   []
  (apache2/install-apache2-action)
  (apache2/install-apachetop-action)
  (gnutls/install-mod-gnutls)
  (jk/install-mod-jk)
  (rewrite/install-mod-rewrite))



(s/defn configure-webserver
  [config :- VhostConfig]
  
  (apache2/config-apache2-production-grade
    :security 
    httpd-config/security)
  
  (if-not (st/get-in config :letsencrypt)
	  (gnutls/configure-gnutls-credentials
	    (st/get-in config :domain-name)
	    (st/get-in config :domain-cert) 
	    (st/get-in config :domain-key) 
	    (st/get-in config :ca-cert)))
  (jk/configure-mod-jk-worker)
  (google/configure-ownership-verification (st/get-in config :id))    
  (apache2/configure-and-enable-vhost
    "000-default"
    (vhost/vhost-conf-default-redirect-to-https-only
      (st/get-in config :domain-name)
      (st/get-in config :server-admin-email) (str "admin@" (st/get-in config :domain-name))))
  
  (apache2/configure-and-enable-vhost
    "000-default-ssl"
    (web/liferay-vhost
      config))
  
    (maintainance/write-maintainance-file :content (st/get-in config :maintainance-page-content))
  )




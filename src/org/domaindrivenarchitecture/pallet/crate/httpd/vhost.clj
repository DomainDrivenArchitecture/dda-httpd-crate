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

(ns org.domaindrivenarchitecture.pallet.crate.httpd.vhost
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
    [org.domaindrivenarchitecture.pallet.crate.httpd.schema :as schema]
  ))

(s/defn vhost-head-wrapper
  "wrapper function for the vhost-head function in the httpd-crate"
  [config :- schema/HttpdConfig]
  ; TODO: krj 2016.05.27: (st/get-in ..) will not work here and needs to be changed
  (vhost/vhost-head (st/get-in config [:listening-port :domain-name :server-admin-email])))

(def vhost-tail-wrapper ["</VirtualHost>"])

; TODO: krj 2016.05.27: needs testing and prob fixing
(s/defn prefix-wrapper
  [config :- schema/HttpdConfig]
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
          (get-in config :google-id)
          (get-in config :consider-jk)
        (maintainance/vhost-service-unavailable-error-page
          (get-in config :consider-jk))
        (vhost/vhost-log 
          :error-name "error.log"
          :log-name "ssl-access.log"
          :log-format "combined")
        (if (get-in config :letsencrypt)
          (gnutls/vhost-gnutls-letsencrypt (get-in config :domain-name))
          (gnutls/vhost-gnutls (get-in config :domain-name)))
        )))))

(s/defn liferay-vhost
  [config :- schema/HttpdConfig]
  (into 
    []
    (concat
      (vhost-head-wrapper config)
      (prefix-wrapper config)
      vhost-tail-wrapper
      )
    )
  )

; TODO: krj 2016.05.27: needs testing and prob fixing
(s/defn configure
  [config :- schema/HttpdConfig]  
  (if-not (get-in config :letsencrypt)
	  (gnutls/configure-gnutls-credentials
	    (get-in config :domain-name)
	    (get-in config :domain-cert) 
	    (get-in config :domain-key) 
	    (get-in config :ca-cert)))
  (jk/configure-mod-jk-worker)
  (google/configure-ownership-verification (get-in config :id))    
  (apache2/configure-and-enable-vhost
    "000-default"
    (vhost/vhost-conf-default-redirect-to-https-only
      (get-in config :domain-name)
      (get-in config :server-admin-email) (str "admin@" (get-in config :domain-name))))
  (apache2/configure-and-enable-vhost
    "000-default-ssl"
    (liferay-vhost
      config))
  )
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
  ; jem: Why should we use a wrapper here?
  (vhost/vhost-head 
    :listening-port (st/get-in config [:listening-port])
    :domain-name  (st/get-in config [:domain-name])
    :server-admin-email (st/get-in config [:server-admin-email])))

(s/defn vhost
  "defines a httpd servers vhost."
  [config :- schema/HttpdConfig]
  (into 
    []
    (let [vhost-config (first (get-in config [:vhosts]))
          use-mod-jk (contains? vhost-config :mod-jk)
          domain-name (get-in vhost-config [:domain-name])]
      (concat
        (vhost/vhost-head 
          :listening-port (st/get-in vhost-config [:listening-port])
          :domain-name  domain-name
          :server-admin-email (st/get-in vhost-config [:server-admin-email]))
        (httpd-common/prefix 
          "  "
          (vhost/vhost-location
            :location-options
            (into []
                  (concat
                    ["Order allow,deny"
                     "Allow from all"
                     ""]
                    (auth/vhost-basic-auth-options
                      :domain-name domain-name))))
          )
        (when (contains? vhost-config :mod-jk)
                         (jk/vhost-jk-mount)) 
        (when (contains? vhost-config :google-id)
          (google/vhost-ownership-verification 
            :id (get-in vhost-config [:google-id])
            :consider-jk use-mod-jk)) 
        (when (contains? vhost-config :maintainance-page-content)
          (maintainance/vhost-service-unavailable-error-page :consider-jk use-mod-jk))
        (vhost/vhost-log 
            :error-name "error.log"
            :log-name "ssl-access.log"
            :log-format "combined")
        (when (contains? vhost-config :cert-letsencrypt)
          (gnutls/vhost-gnutls-letsencrypt domain-name))
        (when (contains? vhost-config :cert-manual)
          (gnutls/vhost-gnutls domain-name))
          vhost/vhost-tail
          ))
    ))

(s/defn configure
  [config :- schema/HttpdConfig]
  (let [vhost-config (first (get-in config [:vhosts]))]  
    (when (contains? vhost-config :cert-manual)
      (gnutls/configure-gnutls-credentials
	       (:domain-name (get-in vhost-config :domain-name))
	       (:domain-cert (get-in vhost-config :domain-cert)) 
	       (:domain-key (get-in vhost-config :domain-key)) 
	       (:ca-cert (get-in vhost-config :ca-cert))))
    ; TODO jem: put timeouts here
    (jk/configure-mod-jk-worker)
    (google/configure-ownership-verification (:id (get-in vhost-config [:id])))    
    (apache2/configure-and-enable-vhost
      "000-default"
      (vhost/vhost-conf-default-redirect-to-https-only
        (:domain-name (get-in vhost-config :domain-name))
        (:adminmail (get-in vhost-config :server-admin-email)) 
        ;(str "admin@" (get-in vhost-config [:domain-name]))
        ))
    (apache2/configure-and-enable-vhost
      "000-default-ssl" (vhost vhost-config))
    ))
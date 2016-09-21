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
    [org.domaindrivenarchitecture.config.commons.map-utils :as map-utils]
    [pallet.actions :as actions]
    [httpd.crate.cmds :as cmds]
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
    [httpd.crate.mod-proxy-http :as proxy]
    [org.domaindrivenarchitecture.pallet.crate.httpd.schema :as schema]
  ))

(s/defn vhost
  "Creates the vhost configuration from a VhostConfig."
  [vhost-config :- schema/VhostConfig]
  (let [use-mod-jk (contains? vhost-config :mod-jk)
        domain-name (get-in vhost-config [:domain-name])]
    (vec 
      (concat
           (vhost/vhost-head 
          :listening-port (get-in vhost-config [:listening-port])
          :domain-name  domain-name
          :server-admin-email (get-in vhost-config [:server-admin-email]))
        (httpd-common/prefix 
          "  " (vec (concat
          (when (contains? vhost-config :location)
            (cond 
              (and (contains? (-> vhost-config :location) :basic-auth)
                   (-> vhost-config :location :basic-auth))
              (vhost/vhost-location
                :path (if (nil? (-> vhost-config :location :path)) 
                        "/"
                        (-> vhost-config :location :path))
                :location-options
                (vec (concat
                       (-> vhost-config :access-control)
                       (auth/vhost-basic-auth-options
                            :domain-name domain-name))))
                 
              (contains? (-> vhost-config :location) :locations-override)
              (-> vhost-config :locations-override)))
          (when (contains? vhost-config :alias)
            (vec
              (for [x (-> vhost-config :alias)]
                (str "Alias " "\"" (-> x :url) "\"" " " "\""(-> x :path)"\"")))) 
          (when (contains? vhost-config :mod-jk)
              (for [x (-> vhost-config :mod-jk :tomcat-forwarding-configuration :mount)]
                (first (jk/vhost-jk-mount :worker (-> x :worker) :path (-> x :path)))))
          (when (contains? (-> vhost-config :mod-jk :tomcat-forwarding-configuration) :unmount)
            (for [x (-> vhost-config :mod-jk :tomcat-forwarding-configuration :unmount)]
              (first (jk/vhost-jk-unmount :worker (-> x :worker) :path (-> x :path)))))
          [" "]
          (when (contains? vhost-config :google-id)
            (google/vhost-ownership-verification 
              :id (get-in vhost-config [:google-id])
              :consider-jk use-mod-jk)) 
          (when (contains? vhost-config :maintainance-page-content)
            (maintainance/vhost-service-unavailable-error-page :consider-jk use-mod-jk))
          (when (contains? vhost-config :proxy)
            (proxy/vhost-proxy 
              :target-port (-> vhost-config :proxy :target-port)
              :additional-directives (-> vhost-config :proxy :additional-directives)))
          (vhost/vhost-log 
            :error-name "error.log"
            :log-name "ssl-access.log"
            :log-format "combined")
          
          (when (contains? vhost-config :cert-letsencrypt)
            (gnutls/vhost-gnutls-letsencrypt domain-name))
          
          (when (contains? vhost-config :cert-manual)
            (gnutls/vhost-gnutls domain-name))
          )))
           vhost/vhost-tail
        (when (contains? vhost-config :mod-jk)
          (concat
            [" "]
            (flatten (for [x (-> vhost-config :mod-jk :worker-properties)]
                          (jk/workers-configuration 
                            :port (-> x :port)
                            :host (-> x :host)
                            :worker (-> x :worker)
                            :socket-connect-timeout-ms (-> x :socket-connect-timeout-ms)
                            :maintain-timout-sec (-> x :maintain-timout-sec)
                            :in-httpd-conf true
                            :socket-keep-alive true
                            :ping-mode "I")))))
    ))))

(s/defn configure-vhost 
  "Takes a vhost-name and vhost-config and generates vhost-config files"
  [vhost-name :- s/Str
   vhost-config :- schema/VhostConfig
   apache-version :- s/Str]
  (when (contains? vhost-config :user-credentials)
      (auth/configure-basic-auth-user-credentials
      :domain-name (-> vhost-config :domain-name)
      :user-credentials (-> vhost-config :user-credentials)))
  (when (contains? vhost-config :cert-manual)
    (gnutls/configure-gnutls-credentials
            :domain-name (-> vhost-config :domain-name)
            :domain-cert (-> vhost-config :cert-manual :domain-cert) 
            :domain-key (-> vhost-config :cert-manual :domain-key) 
            :ca-cert (-> vhost-config :cert-manual :ca-cert)))
    
  (when (contains? vhost-config :cert-letsencrypt)
    (apache2/install-letsencrypt-certs 
      (get-in vhost-config [:domain-name])
      :adminmail (get-in vhost-config [:cert-letsencrypt :letsencrypt-mail])))
  
  (when (contains? vhost-config :google-id)
    (google/configure-ownership-verification :id (get-in vhost-config [:google-id])))
  
  (when (contains? vhost-config :maintainance-page-content)
    (maintainance/write-maintainance-file 
        :content (get-in vhost-config [:maintainance-page-content])))
  
  (apache2/configure-and-enable-vhost
    (str "000-" vhost-name)
    (vhost/vhost-conf-default-redirect-to-https-only
      :domain-name (get-in vhost-config [:domain-name])
      :server-admin-email (get-in vhost-config [:server-admin-email]))
    apache-version)
   
  (apache2/configure-and-enable-vhost
    (str "000-" vhost-name "-ssl") (vhost vhost-config) apache-version)
  )

(s/defn configure
  [config :- schema/HttpdConfig]
  (let [vhost-configs (get-in config [:vhosts])]  
    (doseq [[vhost-name vhost-config] vhost-configs]
      (configure-vhost (name vhost-name) vhost-config (-> config :apache-version)))
    ))
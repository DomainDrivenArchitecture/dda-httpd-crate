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

(ns dda.pallet.crate.dda-httpd-crate.vhost
  (:require
    [schema.core :as s]
    [pallet.actions :as actions]
    [dda.pallet.crate.dda-httpd-crate.letsencrypt :as letsencrypt]
    [dda.pallet.crate.dda-httpd-crate.schema :as schema]
    [httpd.crate.apache2 :as apache2]
    [httpd.crate.basic-auth :as auth]
    [httpd.crate.common :as httpd-common]
    [httpd.crate.google-siteownership-verification :as google]
    [httpd.crate.mod-gnutls :as gnutls]
    [httpd.crate.mod-jk :as jk]
    [httpd.crate.mod-proxy-http :as proxy]
    [httpd.crate.vhost :as vhost]
    [httpd.crate.webserver-maintainance :as maintainance]))

(defn vhost-head
  "This is a wrapper for vhost/vhost-head."
  [vhost-config]
  (vhost/vhost-head
   :listening-port (-> vhost-config :listening-port)
   :domain-name (-> vhost-config :domain-name)
   :server-admin-email (-> vhost-config :server-admin-email)
   :aliases (-> vhost-config :server-aliases)))

(s/defn rewrite-rules
  "Create the rewrite rules for the vhost-function."
  [vhost-config :- schema/VhostConfig]
  (when (contains? vhost-config :rewrite-rules)
    (vec (concat (-> vhost-config :rewrite-rules) [""]))))

(s/defn configure-location
  "Creates the location config for the vhost-function."
  [vhost-config :- schema/VhostConfig]
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
              :domain-name (-> vhost-config :domain-name)))))
      (contains? (-> vhost-config :location) :locations-override)
      (-> vhost-config :locations-override))))

(s/defn create-alias
  "Creates the alias for the vhost-function."
  [vhost-config :- schema/VhostConfig]
  (when (contains? vhost-config :alias)
   (vec
    (for [x (-> vhost-config :alias)]
      (str "Alias " "\"" (-> x :url) "\"" " " "\""(-> x :path)"\"")))))

(s/defn create-mount
  "Create mod-jk mounts for the vhost-function."
  [vhost-config :- schema/VhostConfig]
  (when (contains? vhost-config :mod-jk)
    (for [x (-> vhost-config :mod-jk :tomcat-forwarding-configuration :mount)]
      (first (jk/vhost-jk-mount :worker (-> x :worker) :path (-> x :path))))))

(s/defn create-unmount
  "Create mod-jk unmounts for vhost-function."
  [vhost-config :- schema/VhostConfig]
  (when (contains? (-> vhost-config :mod-jk :tomcat-forwarding-configuration) :unmount)
    (for [x (-> vhost-config :mod-jk :tomcat-forwarding-configuration :unmount)]
      (first (jk/vhost-jk-unmount :worker (-> x :worker) :path (-> x :path))))))

(s/defn create-google-id
  "Create google-id config for vhost-function"
  [vhost-config :- schema/VhostConfig]
  (let [use-mod-jk?  (contains? vhost-config :mod-jk)]
    (when (contains? vhost-config :google-id)
      (if (contains? vhost-config :google-worker)
        (google/vhost-ownership-verification
         :id (get-in vhost-config [:google-id])
         :consider-jk use-mod-jk?
         :worker (get-in vhost-config [:google-worker]))
        (google/vhost-ownership-verification
         :id (get-in vhost-config [:google-id])
         :consider-jk use-mod-jk?)))))

(s/defn create-maintenance-page
  "Create maintanance-page for vhost."
  [vhost-config :- schema/VhostConfig]
  (let [use-mod-jk?  (contains? vhost-config :mod-jk)]
    (when (contains? vhost-config :maintainance-page-content)
      (if (contains? vhost-config :maintainance-page-worker)
        (maintainance/vhost-service-unavailable-error-page
         :consider-jk use-mod-jk?
         :worker (get-in vhost-config [:maintainance-page-worker]))
        (maintainance/vhost-service-unavailable-error-page
         :consider-jk use-mod-jk?)))))

(s/defn create-tomcat-forwarding-configuration
  "Creates the tomcat-forwarding-configuration for the vhosts."
  [vhost-config :- schema/VhostConfig]
  (when (contains? vhost-config :mod-jk)
    (concat
     [" "]
     (flatten
      (for [x (-> vhost-config :mod-jk :worker-properties)]
           (jk/workers-configuration
            :port (-> x :port)
            :host (-> x :host)
            :worker (-> x :worker)
            :socket-connect-timeout-ms (-> x :socket-connect-timeout-ms)
            :maintain-timout-sec (-> x :maintain-timout-sec)
            :in-httpd-conf true
            :socket-keep-alive true
            :ping-mode "I"))))))

(s/defn create-proxy-configuration
  "Creates the proxy configuration for the vhosts."
  [vhost-config :- schema/VhostConfig]
  (when (contains? vhost-config :proxy)
    (proxy/vhost-proxy
     :target-port (-> vhost-config :proxy :target-port)
     :additional-directives (-> vhost-config :proxy :additional-directives))))

(s/defn create-gnutls-letsencrypt-configuration
  "Creates the letsencrpyt configuration for the vhosts."
  [vhost-config :- schema/VhostConfig]
  (gnutls/vhost-gnutls-letsencrypt (-> vhost-config :domain-name)))

(s/defn create-gnutls-cert-contents
  "Creates the vhost-gnutls for the vhosts."
  [vhost-config :- schema/VhostConfig]
  (gnutls/vhost-gnutls (-> vhost-config :domain-name)))

(s/defn create-gnutls-cert-file
  "Creates the vhost-gnutls for the vhosts."
  [vhost-config :- schema/VhostConfig]
  (gnutls/vhost-gnutls-existing
    (get-in vhost-config [:cert-file :domain-cert])
    (get-in vhost-config [:cert-file :domain-key])))

(defn create-vhost-log
  "Create the vhost-log configuration for the vhosts."
  []
  (vhost/vhost-log
   :error-name "error.log"
   :log-name "ssl-access.log"
   :log-format "combined"))

(s/defn vhost
  "Creates the vhost configuration from a VhostConfig."
  [vhost-config :- schema/VhostConfig]
  (let [use-mod-jk (contains? vhost-config :mod-jk)
        domain-name (get-in vhost-config [:domain-name])]
    (vec
     (concat
      (vhost-head vhost-config)
      (httpd-common/prefix
       "  "
       (vec
        (concat
         (rewrite-rules vhost-config)
         (vhost/vhost-document-root (-> vhost-config :document-root))
         (configure-location vhost-config)
         (create-alias vhost-config)
         (create-mount vhost-config)
         (create-unmount vhost-config)
         [" "]
         (create-google-id vhost-config)
         (create-maintenance-page vhost-config)
         (create-proxy-configuration vhost-config)
         (create-vhost-log)
         (when (contains? vhost-config :cert-letsencrypt)
           (create-gnutls-letsencrypt-configuration vhost-config))
         (when (contains? vhost-config :cert-manual)
           (create-gnutls-cert-contents vhost-config))
         (when (contains? vhost-config :cert-file)
            (create-gnutls-cert-file vhost-config)))))
      vhost/vhost-tail
      (create-tomcat-forwarding-configuration vhost-config)))))

(s/defn configure-vhost
  "Takes a vhost-name and vhost-config and generates vhost-config files"
  [vhost-name :- s/Str
   vhost-config :- schema/VhostConfig
   apache-version :- s/Str]
  (when (contains? vhost-config :document-root)
    (actions/directory
      (-> vhost-config :document-root)
      :path true
      :owner "root"
      :group "www-data"
      :mode "550"))
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
    (letsencrypt/configure-letsencrypt-certs
      (get-in vhost-config [:cert-letsencrypt :domains])
      (get-in vhost-config [:cert-letsencrypt :email]))
    (letsencrypt/configure-renew-cron))

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
    (str "000-" vhost-name "-ssl") (vhost vhost-config) apache-version))


(s/defn configure
  [config :- schema/HttpdConfig]
  (let [vhost-configs (get-in config [:vhosts])]
    (doseq [[vhost-name vhost-config] vhost-configs]
      (configure-vhost (name vhost-name) vhost-config (-> config :apache-version)))))

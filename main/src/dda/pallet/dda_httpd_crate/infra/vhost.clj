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

(ns dda.pallet.dda-httpd-crate.infra.vhost
  (:require
    [schema.core :as s]
    [pallet.actions :as actions]
    [dda.pallet.dda-httpd-crate.infra.letsencrypt :as letsencrypt]
    [httpd.crate.apache2 :as apache2]
    [httpd.crate.basic-auth :as auth]
    [httpd.crate.common :as httpd-common]
    [httpd.crate.google-siteownership-verification :as google]
    [httpd.crate.mod-gnutls :as gnutls]
    [httpd.crate.mod-jk :as jk]
    [httpd.crate.mod-proxy-http :as proxy]
    [dda.pallet.dda-httpd-crate.infra.vhost.vhost :as vhost]
    [httpd.crate.webserver-maintainance :as maintainance]))

(def ModJkConfiguration
  {:tomcat-forwarding-configuration
   {:mount [{:path s/Str :worker s/Str}]
    (s/optional-key :unmount) [{:path s/Str :worker s/Str}]}
   :worker-properties [{:worker s/Str
                        :host s/Str
                        :port s/Str
                        :maintain-timout-sec s/Int
                        :socket-connect-timeout-ms s/Int}]})

(def VhostConfig
  "defines a schema for a httpdConfig"
  {:domain-name s/Str
   :listening-port s/Str
   :server-admin-email s/Str
   (s/optional-key :server-aliases) [s/Str]
   (s/optional-key :allow-origin) s/Str
   (s/optional-key :access-control) [s/Str]
   (s/optional-key :document-root) s/Str
   (s/optional-key :rewrite-rules) [s/Str]
   (s/optional-key :user-credentials) [s/Str]
   (s/optional-key :alias) [{:url s/Str :path s/Str}]
   (s/optional-key :alias-match) [{:regex s/Str :path s/Str}]
   (s/optional-key :location) {(s/optional-key :basic-auth) s/Bool
                               (s/optional-key :locations-override) [s/Str]
                               (s/optional-key :path) s/Str}
   ; either letsencrypt or manual certificates
   (s/optional-key :cert-letsencrypt) {:domains [s/Str]
     ; TODO: apply rename refactoring:letsencrypt-mail -> email
                                       :email s/Str}
   (s/optional-key :cert-manual) {:domain-cert s/Str
                                  :domain-key s/Str
                                  (s/optional-key :ca-cert) s/Str}
   (s/optional-key :cert-file) {:domain-cert s/Str
                                :domain-key s/Str
                                (s/optional-key :ca-cert) s/Str}
   ; mod_jk
   (s/optional-key :mod-jk) ModJkConfiguration
   ;proxy
   (s/optional-key :proxy) {:target-port s/Str
                            :additional-directives [s/Str]}

   ; other stuff
   (s/optional-key :maintainance-page-content) [s/Str]
   (s/optional-key :maintainance-page-worker) s/Str
   (s/optional-key :google-id) s/Str
   (s/optional-key :google-worker) s/Str})

(def AllVhostConfig
  {s/Keyword VhostConfig})

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
  [vhost-config :- VhostConfig]
  (when (contains? vhost-config :rewrite-rules)
    (vec (concat (-> vhost-config :rewrite-rules) [""]))))

(s/defn location
  "Creates the location config for the vhost-function."
  [vhost-config :- VhostConfig]
  (let [{:keys [location access-control domain-name]} vhost-config
        {:keys [basic-auth path locations-override]
         :or {path "/"}} location]
    (println location)
    (cond
      (and (some? location)
           basic-auth)
      (vhost/vhost-location
        :path path
        :location-options (vec (concat
                                  access-control
                                  (auth/vhost-basic-auth-options
                                    :domain-name domain-name))))
      (and (some? location)
           locations-override)
      (vhost/vhost-location
        :path path
        :location-options locations-override)
      (some? location)
      (vhost/vhost-location
        :path path)
      :default
      [])))

(s/defn create-alias
  "Creates the alias for the vhost-function."
  [vhost-config :- VhostConfig]
  (when (contains? vhost-config :alias)
   (vec
    (for [x (-> vhost-config :alias)]
      (str "Alias " "\"" (-> x :url) "\"" " " "\""(-> x :path)"\"")))))

(s/defn
  create-allow-origin
  "Creates the alias for the vhost-function."
  [allow-origin :- s/Str]
  [(str "Header set Access-Control-Allow-Origin \"" allow-origin "\"")
   ""])

(s/defn create-alias-match
  "Creates the alias match for regex for the vhost-function."
  [vhost-config :- VhostConfig]
  (vec
    (for [x (-> vhost-config :alias-match)]
      (str "AliasMatch " "\"" (-> x :regex) "\"" " " "\""(-> x :path)"\""))))

(s/defn create-mount
  "Create mod-jk mounts for the vhost-function."
  [vhost-config :- VhostConfig]
  (when (contains? vhost-config :mod-jk)
    (for [x (-> vhost-config :mod-jk :tomcat-forwarding-configuration :mount)]
      (first (jk/vhost-jk-mount :worker (-> x :worker) :path (-> x :path))))))

(s/defn create-unmount
  "Create mod-jk unmounts for vhost-function."
  [vhost-config :- VhostConfig]
  (when (contains? (-> vhost-config :mod-jk :tomcat-forwarding-configuration) :unmount)
    (for [x (-> vhost-config :mod-jk :tomcat-forwarding-configuration :unmount)]
      (first (jk/vhost-jk-unmount :worker (-> x :worker) :path (-> x :path))))))

(s/defn create-google-id
  "Create google-id config for vhost-function"
  [vhost-config :- VhostConfig]
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
  [vhost-config :- VhostConfig]
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
  [vhost-config :- VhostConfig]
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
  [vhost-config :- VhostConfig]
  (when (contains? vhost-config :proxy)
    (proxy/vhost-proxy
     :target-port (-> vhost-config :proxy :target-port)
     :additional-directives (-> vhost-config :proxy :additional-directives))))

(s/defn create-gnutls-letsencrypt-configuration
  "Creates the letsencrpyt configuration for the vhosts."
  [vhost-config :- VhostConfig]
  (gnutls/vhost-gnutls-letsencrypt (-> vhost-config :domain-name)))

(s/defn create-gnutls-cert-contents
  "Creates the vhost-gnutls for the vhosts."
  [vhost-config :- VhostConfig]
  (gnutls/vhost-gnutls (-> vhost-config :domain-name)))

(s/defn create-gnutls-cert-file
  "Creates the vhost-gnutls for the vhosts."
  [vhost-config :- VhostConfig]
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
  [vhost-config :- VhostConfig]
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
         (location vhost-config)
         (create-alias vhost-config)
         (create-alias-match vhost-config)
         (when (contains? vhost-config :allow-origin)
           (create-allow-origin (:allow-origin vhost-config)))
         (create-mount vhost-config)
         (create-unmount vhost-config)
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

(s/defn install-vhost
  "Takes a vhost-name and vhost-config and generates vhost-config files"
  [vhost-name :- s/Str
   vhost-config :- VhostConfig
   apache-version :- s/Str]
  (when (contains? vhost-config :cert-letsencrypt)
    (letsencrypt/install-letsencrypt-certs
      (get-in vhost-config [:cert-letsencrypt :domains])
      (get-in vhost-config [:cert-letsencrypt :email]))
    (letsencrypt/configure-renew-cron)))

(s/defn configure-vhost
  "Takes a vhost-name and vhost-config and generates vhost-config files"
  [vhost-name :- s/Str
   vhost-config :- VhostConfig
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
  (when (contains? vhost-config :google-id)
    (google/configure-ownership-verification :id (get-in vhost-config [:google-id])))
  (when (contains? vhost-config :maintainance-page-content)
    (maintainance/write-maintainance-file
        :content (get-in vhost-config [:maintainance-page-content])))
  (apache2/configure-and-enable-vhost
    (str "000-" vhost-name)
    (vhost/vhost-conf-default-redirect-to-https-only
      :domain-name (get-in vhost-config [:domain-name])
      :document-root-path "/var/www/html"
      :server-admin-email (get-in vhost-config [:server-admin-email]))
    apache-version)
  (apache2/configure-and-enable-vhost
    (str "000-" vhost-name "-ssl") (vhost vhost-config) apache-version))

(s/defn install
  [apache-version :- s/Str
   vhost-configs :- AllVhostConfig]
  (doseq [[vhost-name vhost-config] vhost-configs]
    (install-vhost (name vhost-name) vhost-config apache-version)))

(s/defn configure
  [apache-version :- s/Str
   vhost-configs :- AllVhostConfig]
  (letsencrypt/configure-renew-cron)
  (doseq [[vhost-name vhost-config] vhost-configs]
    (configure-vhost (name vhost-name) vhost-config apache-version)))

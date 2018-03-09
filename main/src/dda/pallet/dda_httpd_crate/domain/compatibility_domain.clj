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
(ns dda.pallet.dda-httpd-crate.domain.compatibility-domain
  (:require
   [dda.pallet.dda-httpd-crate.infra :as infra]
   [dda.pallet.dda-httpd-crate.domain.schema :as domain-schema]
   [dda.pallet.dda-httpd-crate.infra.schema :as httpd-schema]
   [schema.core :as s]))

(def VhostDomainConfig
  {:domain-name s/Str
   (s/optional-key :listening-port) s/Str
   (s/optional-key :server-admin-email) s/Str
   (s/optional-key :access-control) [s/Str]
   ;;definitly optional
   (s/optional-key :document-root) s/Str
   (s/optional-key :rewrite-rules) [s/Str]
   (s/optional-key :user-credentials) [s/Str]
   (s/optional-key :alias) [{:url s/Str :path s/Str}]
   (s/optional-key :location) {(s/optional-key :basic-auth) s/Bool
                               (s/optional-key :locations-override) [s/Str]
                               (s/optional-key :path) s/Str}
   (s/optional-key :cert-letsencrypt) {:domains [s/Str]
                                       :email s/Str}
   (s/optional-key :cert-manual) {:domain-cert s/Str
                                  :domain-key s/Str
                                  (s/optional-key :ca-cert) s/Str}
   (s/optional-key :proxy) {:target-port s/Str
                            :additional-directives [s/Str]}
   (s/optional-key :maintainance-page-content) [s/Str]
   (s/optional-key :maintainance-page-worker) s/Str
   (s/optional-key :google-id) s/Str
   (s/optional-key :google-worker) s/Str
   (s/optional-key :mod-jk) httpd-schema/mod-jk-configuration})

(def HttpdDomainConfig
  {:compat
   {(s/optional-key :apache-version) s/Str
    (s/optional-key :jk-configuration) httpd-schema/jk-configuration
    :vhosts {s/Keyword VhostDomainConfig}
    (s/optional-key :limits) {(s/optional-key :server-limit) s/Int
                              (s/optional-key :max-clients) s/Int}
    (s/optional-key :apache-modules) {(s/optional-key :a2enmod) [s/Str]}}})

; TODO: add schema description to function signature
(defn create-vhost-stack-config-from-domain
  "Creates a vhost stack configuration from a vhost-convention config."
  [vhost-convention-config]
  (-> vhost-convention-config
      (assoc :listening-port (or (:listening-port vhost-convention-config) "443"))
      (assoc :server-admin-email (or
                                  (:server-admin-email vhost-convention-config)
                                  "admin@localdomain"))
      (assoc :access-control
             (or
              (:access-control vhost-convention-config)
              ["Order allow,deny" "Allow from all" ""]))))

; TODO: add schema description to function signature
(defn create-stack-vhost-config
  "Creates a stack vhost configuration from a httpd-domain-config."
  [httpd-domain-config]
  (into {}
   (for [[k v] (:vhosts httpd-domain-config)]
     {k (create-vhost-stack-config-from-domain v)})))

; TODO: add schema description to function signature
(defn create-stack-limits
  "Creates stack-limits-configuration"
  [convention-config]
  {:server-limit (or (-> convention-config :limits :server-limit) 150)
   :max-clients (or (-> convention-config :limits :max-clients) 150)})

; TODO: add schema description to function signature
(defn create-stack-jk-configuration
  "Creates the stack-jk-configuration"
  [convention-config]
  {:jkStripSession (or (-> convention-config :jk-configuration :jkStripSession) "On")
   :jkWatchdogInterval (or (-> convention-config :jk-configuration :jkWatchdogInterval) 120)})

(s/defn crate-configuration :- infra/HttpdConfig
  "Creates a complete infrastructure config from a domain-config."
  [domain-config :- HttpdDomainConfig]
  (let [convention-config (:compat domain-config)
        vhosts (:vhosts convention-config)]
    (-> convention-config
        (assoc :apache-version (or (:apache-version convention-config) "2.4"))
        (assoc :jk-configuration (create-stack-jk-configuration convention-config))
        (assoc :vhosts (create-stack-vhost-config convention-config))
        (assoc :limits (create-stack-limits convention-config)))))

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
(ns dda.pallet.dda-httpd-crate.domain.jk
  (:require
    [clojure.string :as st]
    [schema.core :as s]
    [pallet.api :as api]
    [dda.config.commons.map-utils :as mu]
    [dda.pallet.dda-httpd-crate.infra :as infra]
    [dda.pallet.dda-httpd-crate.domain.maintainance :as maintain]
    [dda.pallet.dda-httpd-crate.domain.domain-name :as domain-name]
    [dda.pallet.dda-httpd-crate.domain.schema :as domain-schema]))

(def server-config
  {:apache-version "2.4"
   :limits {:server-limit 150
            :max-clients 150}
   :settings #{:name-based}
   :jk-configuration {:jkStripSession "On",
                      :jkWatchdogInterval 120}})

(s/defn ^:always-validate
  infra-vhost-configuration :- infra/VhostConfig
  [jk-config :- domain-schema/JkConfig]
  (let [domain-config (:jk jk-config)
        {:keys [domain-name google-id settings]} domain-config]
      (merge
        {:domain-name domain-name}
        (if (domain-name/root-domain? domain-name)
          {:server-aliases [(str "www." domain-name)]}
          {})
        {:listening-port "443"
         :document-root (str "/var/www/" domain-name)
         :server-admin-email (str "admin@" (domain-name/calculate-root-domain domain-name))
         :mod-jk {:tomcat-forwarding-configuration {:mount [{:path "/*" :worker "mod_jk_www"}]
                                                    :unmount [{:path "/error/*" :worker "mod_jk_www"}]}
                  :worker-properties [{:worker "mod_jk_www"
                                       :host "localhost"
                                       :port "8009"
                                       :maintain-timout-sec 90
                                       :socket-connect-timeout-ms 62000}]}}
        (maintain/infra-maintainance-configuration settings domain-name)
        (if (contains? domain-config :google-id)
          {:google-id google-id}
          {})
        (if (contains? settings :test)
          {:cert-file {:domain-cert "/etc/ssl/certs/ssl-cert-snakeoil.pem"
                       :domain-key "/etc/ssl/private/ssl-cert-snakeoil.key"}}
          {:cert-letsencrypt {:domains (domain-name/calculate-domains domain-name)
                              :email (str "admin@" (domain-name/calculate-root-domain domain-name))}}))))

(s/defn ^:always-validate
  infra-configuration :- infra/HttpdConfig
  [jk-config :- domain-schema/JkConfig]
  (merge
    server-config
    {:vhosts
      {:default (infra-vhost-configuration jk-config)}}))

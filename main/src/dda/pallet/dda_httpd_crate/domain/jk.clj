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
    [dda.pallet.dda-httpd-crate.domain.generic-vhost :as generic-vhost]))

(def JkConfig
  {:jk
   (merge
     generic-vhost/VhostConfig
     {:domain-name s/Str})})

(def server-config
  {:apache-version "2.4"
   :limits {:server-limit 150
            :max-clients 150}
   :settings #{:name-based}
   :jk-configuration {:jkStripSession "On",
                      :jkWatchdogInterval 120}})

(s/defn
  infra-vhost-configuration :- infra/VhostConfig
  [jk-config :- JkConfig]
  (let [domain-config (:jk jk-config)
        {:keys [domain-name google-id settings]} domain-config]
      (merge
        (generic-vhost/infra-vhost-configuration domain-name domain-config)
        {:mod-jk {:tomcat-forwarding-configuration {:mount [{:path "/*" :worker "mod_jk_www"}]
                                                    :unmount [{:path "/error/*" :worker "mod_jk_www"}]}
                  :worker-properties [{:worker "mod_jk_www"
                                       :host "localhost"
                                       :port "8009"
                                       :maintain-timout-sec 90
                                       :socket-connect-timeout-ms 62000}]}})))

(s/defn
  infra-configuration :- infra/HttpdConfig
  [jk-config :- JkConfig]
  (merge
    server-config
    {:vhosts
      {:default (infra-vhost-configuration jk-config)}}))

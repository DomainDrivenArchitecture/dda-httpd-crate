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

(ns dda.pallet.dda-httpd-crate.domain.tomcat
  (:require
    [clojure.string :as st]
    [schema.core :as s]
    [pallet.api :as api]
    [dda.pallet.dda-httpd-crate.infra :as infra]
    [dda.pallet.dda-httpd-crate.domain.generic-vhost :as generic-vhost]))

;; REVIEW jem 2018_12_12: Pls. merge with jk & deprecate either tomcat or jk.
(def TomcatConfig
  {:tomcat
   (merge
     generic-vhost/VhostConfig
     {:domain-name s/Str
      (s/optional-key :jk-mount) [{:path s/Str :worker s/Str}]
      (s/optional-key :jk-unmount) [{:path s/Str :worker s/Str}]})})

(def server-config
  {:apache-version "2.4"
   :limits {:server-limit 150
            :max-clients 150}
   :settings #{:name-based}
   :jk-configuration {:jkStripSession "On",
                      :jkWatchdogInterval 120}})

(s/defn
  infra-vhost-configuration :- infra/VhostConfig
  [tomcat-config :- TomcatConfig]
  (let [domain-config (:tomcat tomcat-config)
        {:keys [domain-name google-id alias jk-mount jk-unmount settings]} domain-config]
      (merge
        (generic-vhost/infra-vhost-configuration domain-name domain-config)
        {:mod-jk {:tomcat-forwarding-configuration
                  (merge
                    {:mount (conj (if (contains? domain-config :jk-mount)
                                      jk-mount
                                      [])
                                  {:path "/*" :worker "mod_jk_www"})}
                    (if (contains? domain-config :jk-unmount)
                        {:unmount jk-unmount}
                        {}))
                  :worker-properties [{:worker "mod_jk_www"
                                       :host "localhost"
                                       :port "8009"
                                       :maintain-timout-sec 90
                                       :socket-connect-timeout-ms 62000}]}})))

(s/defn
  infra-configuration :- infra/HttpdConfig
  [domain-config :- TomcatConfig]
  (merge
      server-config
      {:vhosts
       {:default (infra-vhost-configuration domain-config)}}))

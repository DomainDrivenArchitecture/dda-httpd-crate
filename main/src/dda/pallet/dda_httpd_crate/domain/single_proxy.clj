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
(ns dda.pallet.dda-httpd-crate.domain.single-proxy
  (:require
    [clojure.string :as st]
    [schema.core :as s]
    [pallet.api :as api]
    [dda.pallet.dda-httpd-crate.infra :as infra]
    [dda.pallet.dda-httpd-crate.domain.domain-name :as dn]
    [dda.pallet.dda-httpd-crate.domain.generic-vhost :as generic-vhost]))

(def SingleProxyValueConfig
  (merge
    {:domain-name s/Str}
    generic-vhost/VhostConfig))

(def SingleProxyConfig
  {:single-proxy SingleProxyValueConfig})

(def server-config
  {:apache-version "2.4"
   :limits {:server-limit 150
            :max-clients 150}
   :settings #{:name-based}})

(s/defn
  infra-vhost-configuration :- infra/VhostConfig
  [domain-config :- SingleProxyValueConfig]
  (let [{:keys [domain-name google-id settings alias alias-match
                allow-origin]} domain-config]
      (merge
        (generic-vhost/infra-vhost-configuration domain-name domain-config)
        {:proxy {:target-port "8080"
                 :additional-directives ["ProxyPreserveHost On"
                                         "ProxyRequests     Off"]}})))

(s/defn
  infra-configuration :- infra/HttpdConfig
  [single-config :- SingleProxyConfig]
  (let [domain-config (:single-proxy single-config)
        {:keys [domain-name google-id settings]} domain-config]
    (merge
      server-config
      {:vhosts
       {:default (infra-vhost-configuration domain-config)}})))

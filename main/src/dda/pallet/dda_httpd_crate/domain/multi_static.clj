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
(ns dda.pallet.dda-httpd-crate.domain.multi-static
  (:require
    [schema.core :as s]
    [pallet.api :as api]
    [dda.pallet.dda-httpd-crate.infra :as infra]
    [dda.pallet.dda-httpd-crate.domain.single-static :as single]
    [dda.pallet.dda-httpd-crate.domain.schema :as domain-schema]))

(s/defn transform-static-vhost
  [element :- [s/Keyword domain-schema/VhostConfig]]
  (let [[domain-key vhost-config] element
        domain-name (name domain-key)
        {:keys [google-id settings]} vhost-config]
    {domain-key
     (single/infra-vhost-configuration (merge
                                          {:domain-name domain-name}
                                          element))}))

(s/defn infra-configuration :- infra/HttpdConfig
  [domain-config :- domain-schema/MultiStaticConfig]
  (merge
    single/server-config
    {:vhosts
     (reduce-kv
      (fn [m k v] (merge m v))
      {}
      (into [] (map transform-static-vhost domain-config)))}))

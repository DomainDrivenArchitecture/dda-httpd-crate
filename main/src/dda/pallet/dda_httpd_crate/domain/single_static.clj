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
(ns dda.pallet.dda-httpd-crate.domain.single-static
  (:require
    [clojure.string :as st]
    [schema.core :as s]
    [pallet.api :as api]
    [dda.pallet.dda-httpd-crate.infra :as infra]
    [dda.pallet.dda-httpd-crate.domain.maintainance :as maintain]
    [dda.pallet.dda-httpd-crate.domain.generic-vhost :as generic-vhost]))

(def SingleStaticValueConfig
  (merge
    {:domain-name s/Str}
    generic-vhost/VhostConfig
    {(s/optional-key :settings)
     (hash-set (s/enum :test
                       :without-maintainance
                       :with-php))}))

(def SingleStaticConfig
  {:single-static SingleStaticValueConfig})

(def server-config
  {:apache-version "2.4"
   :limits {:server-limit 150
            :max-clients 150}
   :settings #{:name-based}})

(s/defn
  infra-vhost-configuration :- infra/VhostConfig
  [domain-config :- SingleStaticValueConfig]
  (let [{:keys [domain-name google-id settings alias alias-match
                allow-origin]} domain-config]
      (merge
        (generic-vhost/infra-vhost-configuration domain-name domain-config)
        (when (contains? settings :with-php)
           {:location
             {:locations-override
               ["Options FollowSymLinks" "AllowOverride All"]}}))))

(s/defn
  infra-configuration :- infra/HttpdConfig
  [single-config :- SingleStaticConfig]
  (let [domain-config (:single-static single-config)
        {:keys [domain-name google-id settings]} domain-config]
    (merge
      server-config
      (if (contains? settings :with-php)
        {:apache-modules {:install ["libapache2-mod-php7.0"]
                          :a2enmod ["php7.0"]}}
        {})
      {:vhosts
       {:default (infra-vhost-configuration domain-config)}})))

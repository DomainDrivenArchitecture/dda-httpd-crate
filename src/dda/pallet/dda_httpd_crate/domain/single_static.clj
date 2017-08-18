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
    [dda.pallet.dda-httpd-crate.domain.schema :as domain-schema]))

(def server-config
  {:apache-version "2.4"
   :limits {:server-limit 150
            :max-clients 150}
   :settings #{:name-based}})

(defn root-domain? [domain-name]
  (<= (count (st/split domain-name #"\."))
      2))

(defn calculate-domains [domain-name]
  (if (root-domain? domain-name)
    [domain-name (str "www." domain-name)]
    [domain-name]))

(defn calculate-root-domain [domain-name]
  (let [parts (st/split domain-name #"\.")
        length (count parts)]
    (str (nth parts (- length 2)) "." (nth parts (- length 1)))))

(s/defn infra-vhost-configuration :- infra/VhostConfig
  [domain-config :- domain-schema/SingleStaticConfig]
  (let [{:keys [domain-name google-id settings]} domain-config]
      (merge
        {:domain-name domain-name}
        (if (root-domain? domain-name)
          {:server-aliases [(str "www." domain-name)]}
          {})
        {:listening-port "443"
         :document-root (str "/var/www/" domain-name)
         :server-admin-email (str "admin@" (calculate-root-domain domain-name))}
        (if (contains? settings :with-php)
           {:location
             {:locations-override
               ["Options FollowSymLinks" "AllowOverride All"]}}
           {})
        (if (contains? settings :without-maintainance)
         {}
         {:maintainance-page-content (maintain/maintainance-html domain-name)})
        (if (contains? domain-config :google-id)
          {:google-id google-id}
          {})
        (if (contains? settings :test)
          {:cert-file {:domain-cert "/etc/ssl/certs/ssl-cert-snakeoil.pem"
                       :domain-key "/etc/ssl/private/ssl-cert-snakeoil.key"}}
          {:cert-letsencrypt {:domains (calculate-domains domain-name)
                              :email (str "admin@" (calculate-root-domain domain-name))}}))))

(s/defn infra-configuration :- infra/HttpdConfig
  [domain-config :- domain-schema/SingleStaticConfig]
  (let [{:keys [domain-name google-id settings]} domain-config]
    (merge
      server-config
      (if (contains? settings :with-php)
        {:apache-modules {:a2enmod ["mod-php"]}}
        {})
      {:vhosts
       {:default (infra-vhost-configuration domain-config)}})))

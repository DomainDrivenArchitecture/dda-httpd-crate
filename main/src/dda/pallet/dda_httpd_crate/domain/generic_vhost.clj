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
(ns dda.pallet.dda-httpd-crate.domain.generic-vhost
  (:require
    [schema.core :as s]
    [clojure.string :as st]
    [dda.pallet.dda-httpd-crate.domain.maintainance :as maintain]))

(def VhostSettings  (hash-set (s/enum :test
                                      :without-maintainance)))

(def VhostConfig
  {(s/optional-key :google-id) s/Str
   (s/optional-key :allow-origin) s/Str
   (s/optional-key :alias) [{:url s/Str :path s/Str}]
   (s/optional-key :alias-match) [{:regex s/Str :path s/Str}]
   (s/optional-key :settings) VhostSettings})

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

(s/defn
  infra-vhost-configuration
  [domain-name :- s/Str
   domain-config] ;:- we get more input ... VhostConfig]
  (let [{:keys [google-id settings alias alias-match
                allow-origin]} domain-config]
      (merge
        {:domain-name domain-name}
        (when (root-domain? domain-name)
          {:server-aliases [(str "www." domain-name)]})
        {:listening-port "443"
         :document-root (str "/var/www/" domain-name)
         :server-admin-email (str "admin@" (calculate-root-domain domain-name))}
        (maintain/infra-maintainance-configuration settings domain-name)
        (when (contains? domain-config :google-id)
          {:google-id google-id})
        (when (contains? domain-config :alias)
          {:alias alias})
        (when (contains? domain-config :alias-match)
          {:alias-match alias-match})
        (when (contains? domain-config :allow-origin)
          {:allow-origin allow-origin})
        (if (contains? settings :test)
          {:cert-file {:domain-cert "/etc/ssl/certs/ssl-cert-snakeoil.pem"
                       :domain-key "/etc/ssl/private/ssl-cert-snakeoil.key"}}
          {:cert-letsencrypt {:domains (calculate-domains domain-name)
                              :email (str "admin@" (calculate-root-domain domain-name))}}))))

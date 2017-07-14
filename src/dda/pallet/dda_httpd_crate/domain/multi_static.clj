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
    [dda.pallet.dda-httpd-crate.domain.schema :as domain-schema]))

(def VhostConfig
  {(s/optional-key :google-id) s/Str
   (s/optional-key :settings) (hash-set (s/enum :test))})

(def MultiStaticConfig
  {s/Keyword VhostConfig})

(defn maintainance-html [name]
 ["<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">"
  "<html>"
  "<head>"
  (str "<title>" name " maintainance</title>")
  "<meta name=\"ROBOTS\" content=\"NOINDEX, NOFOLLOW\">"
  "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">"
  "<meta http-equiv=\"content-type\" content=\"application/xhtml+xml; charset=UTF-8\">"
  "<meta http-equiv=\"content-style-type\" content=\"text/css\">"
  "<meta http-equiv=\"expires\" content=\"0\">"
  "  <style type=\"text/css\">"
  "    * {background-color: #EEF0F2}"
  "  </style>"
  "</head>"
  "<body>"
  "  <center>"
  "    <h1>Maintainance ongoing</h1>"
  "    <h2>At the moment we're down due to do some maintainance. Please retry in a view moments.</h2>"
  "  </center>"
  "</body>"
  "</html>"])

(s/defn transform-static-vhost
  [element :- [s/Keyword VhostConfig]]
  (let [[domain-key vhost-config] element
        domain-name (name domain-key)
        {:keys [google-id settings]} vhost-config]
    {domain-key
     (merge
       {:domain-name domain-name
        :server-aliases [(str "www." domain-name)]
        :listening-port "443"
        :document-root (str "/var/www/" domain-name)
        :server-admin-email (str "admin@" domain-name)
        :maintainance-page-content (maintainance-html domain-name)}
       (if (contains? vhost-config :google-id)
         {:google-id google-id}
         {})
       (if (contains? settings :test)
         {:cert-file {:domain-cert "/etc/ssl/certs/ssl-cert-snakeoil.pem"
                      :domain-key "/etc/ssl/private/ssl-cert-snakeoil.key"}}
         {:cert-letsencrypt {:domains [domain-name (str "www." domain-name)]
                                :email (str "admin@" domain-name)}}))}))

(s/defn crate-configuration :- infra/HttpdConfig
  [domain-config :- MultiStaticConfig]
  {:apache-version "2.4"
   :limits {:server-limit 150
            :max-clients 150}
   :settings #{:name-based}
   :vhosts
   (reduce-kv
    (fn [m k v] (merge m v))
    {}
    (into [] (map transform-static-vhost domain-config)))})

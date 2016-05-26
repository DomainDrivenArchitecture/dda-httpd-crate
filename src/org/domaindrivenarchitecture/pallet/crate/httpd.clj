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

(ns org.domaindrivenarchitecture.pallet.crate.httpd
  (:require
    [schema.core :as s]
    [schema-tools.core :as st]
    [org.domaindrivenarchitecture.config.commons.map-utils :as map-utils]
  ))


(def HttpdConfig
  "defines a schema for a httpdConfig"
  {:fqdn s/Str
   :app-port s/Str
   :maintainance-page-content [s/Str]
   (s/optional-key :httpd)
   (s/conditional
       #(= (:letsencrypt %) true)
       {:letsencrypt (s/eq true) 
        :letsencrypt-mail s/Str
        :fqdn s/Str
        (s/optional-key :app-port) s/Str
        (s/optional-key :google-id) s/Str
        (s/optional-key :maintainance-page-content) [s/Str]}
       #(= (:letsencrypt %) false)
       {:letsencrypt (s/eq false) 
        :domain-cert s/Str 
        :domain-key s/Str 
        (s/optional-key :ca-cert) s/Str
        :fqdn s/Str
        (s/optional-key :app-port) s/Str
        (s/optional-key :google-id) s/Str
        (s/optional-key :maintainance-page-content) [s/Str]})
   })


(def default-httpd-webserver-configuration
  {; Webserver Configuration
   :letsencrypt true
   :fqdn "localhost.localdomain"
   :app-port "8009"
   :maintainance-page-content ["<h1>Webserver Maintainance Mode</h1>"]})

; TODO: review jem: 2016_05_25: we will need some merge-config function.
(s/defn ^:always-validate merge-config :- HttpdConfig
  "merges the partial config with default config & ensures that resulting config is valid."
  [partial-config]
  (map-utils/deep-merge default-httpd-webserver-configuration partial-config))






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
    [org.domaindrivenarchitecture.pallet.core.dda-crate :as dda-crate]
    [org.domaindrivenarchitecture.config.commons.map-utils :as map-utils]
    [org.domaindrivenarchitecture.pallet.crate.httpd.server :as server]
    [org.domaindrivenarchitecture.pallet.crate.httpd.vhost :as vhost]))


(def HttpdConfig
  "defines a schema for a httpdConfig"
  {(s/conditional
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


;TODO: verify types
; TODO: review jem 2016.05.27: I expect this to be quite the same as HttpConfig - but it will be good to refactor this out.
(def VhostConfig
  "defindes a schema for a VhostConfig"
  {:listening-port s/Str 
   :ca-cert s/Any
   :domain-name s/Any
   :domain-cert s/Any
   :domain-key s/Any
   :server-admin-email s/Str
   :google-id s/Str
   :consider-jk s/Bool
   :letsencrypt s/Bool
   :maintainance-page-content [s/Str]
   })

(def default-config
  {:httpd {; Webserver Configuration
           :letsencrypt true
           :fqdn "localhost.localdomain"
           :app-port "8009"
           :maintainance-page-content ["<h1>Webserver Maintainance Mode</h1>"]}})

(s/defn ^:always-validate merge-config :- HttpdConfig
  "merges the partial config with default config & ensures that resulting config is valid."
  [partial-config]
  (map-utils/deep-merge default-httpd-webserver-configuration partial-config))

(s/defmethod dda-crate/dda-install :dda-httpd [dda-crate partial-effective-config]
  ; TODO: review jem 2016.05.27: We should pull the merge-config to dda-pallet also ... in this case we get 
  ; the full effectife config here
  (let [config (merge-config partial-effective-config)]
    (server/install)))

(s/defmethod dda-crate/dda-configure :dda-httpd 
  [dda-crate :- ddaCrate/DdaCrate 
   partial-effective-config]
  (let [config (merge-config partial-effective-config)]
    (server/configure)
    (vhost/configure)))

(def with-httpd
  (let 
    [init-crate (dda-crate/make-dda-crate
                  :facility :dda-httpd
                  :version [0 1 0]
                  :config-default default-config
                  :config-schema HttpdConfig)]
    (dda-crate/create-server-spec init-crate)
    ))




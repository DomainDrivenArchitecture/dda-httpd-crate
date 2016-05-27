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
    [org.domaindrivenarchitecture.pallet.core.dda-crate :as dda-crate]
    [org.domaindrivenarchitecture.config.commons.map-utils :as map-utils]
    [org.domaindrivenarchitecture.pallet.crate.httpd.schema :as schema]
    [org.domaindrivenarchitecture.pallet.crate.httpd.server :as server]
    [org.domaindrivenarchitecture.pallet.crate.httpd.vhost :as vhost]))

(def HttpdConfig schema/HttpdConfig)

(def default-config
  {:letsencrypt true
   :fqdn "localhost.localdomain"
   :app-port "8009"
   :maintainance-page-content ["<h1>Webserver Maintainance Mode</h1>"]})

(s/defn ^:always-validate merge-config :- HttpdConfig
  "merges the partial config with default config & ensures that resulting config is valid."
  [partial-config]
  (map-utils/deep-merge vhost/default-httpd-webserver-configuration partial-config))

(s/defmethod dda-crate/dda-install :dda-httpd [dda-crate partial-effective-config]
  ; TODO: review jem 2016.05.27: We should pull the merge-config to dda-pallet also ... in this case we get 
  ; the full effectife config here
  (let [config (merge-config partial-effective-config)]
    (server/install config)))

(s/defmethod dda-crate/dda-configure :dda-httpd 
  [dda-crate :- dda-crate/DdaCrate ;TODO: check type
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




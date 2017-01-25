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
    [org.domaindrivenarchitecture.pallet.core.dda-crate.versioned-plan :as version-plan]
    [org.domaindrivenarchitecture.pallet.crate.httpd.vhost :as vhost]))

(def HttpdConfig schema/HttpdConfig)

(def default-vhost-config
  {:domain-name "localhost.localdomain"
   :listening-port "443"
   :server-admin-email "admin@localdomain"
   :maintainance-page-content ["<h1>Webserver Maintainance Mode</h1>"]
   :access-control  ["Order allow,deny" "Allow from all" ""]
   :mod-jk {:tomcat-forwarding-configuration {:mount [{:path "/*" :worker "mod_jk_www"}]
                                              ;a default for unmount is not recommended
                                              ;:unmount [{:path "/*" :worker "mod_jk_www"}]
                                              }
            :worker-properties [{:worker "mod_jk_www"
                                 :host "127.0.0.1"
                                 :port "8009"
                                 :maintain-timout-sec 90
                                 :socket-connect-timeout-ms 62000}]}})

(def default-config
  {:apache-version "2.4"
   :limits {:server-limit 150 
            :max-clients 150}
   :jk-configuration {:jkStripSession "On"
                      :jkWatchdogInterval 120}
   :vhosts {:default default-vhost-config}
   })

(def dda-httpd-crate 
  (dda-crate/make-dda-crate
    :facility :dda-httpd
    :version [0 1 0]
    :config-schema HttpdConfig
    :config-default default-config
    ))

(s/defn install
  "install function for httpd-crate."
  [config :- HttpdConfig]
  (server/install config))

(s/defn configure
  "configure function for httpd-crate."
  [config :- HttpdConfig]
  (server/configure config)
  (vhost/configure config))

; TODO: review jem 2016_06_28: we don't need verions here, I think. Let's discuss this.
(defmethod dda-crate/dda-install 
  :dda-httpd [dda-crate partial-effective-config]
  (let [config (dda-crate/merge-config dda-crate partial-effective-config)]
    (version-plan/plan-when-cleaninstall
      dda-crate
      (install config))))

(defmethod dda-crate/dda-configure 
  :dda-httpd [dda-crate partial-effective-config]
  (let [config (dda-crate/merge-config dda-crate partial-effective-config)]
    (configure config)))

(def with-httpd
  (dda-crate/create-server-spec dda-httpd-crate))




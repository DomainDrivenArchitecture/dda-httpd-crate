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

(ns org.domaindrivenarchitecture.pallet.crate.httpd.server
  (:require
    [schema.core :as s]
    [schema-tools.core :as st]
    [pallet.actions :as actions]
    [httpd.crate.apache2 :as apache2]
    [httpd.crate.config :as httpd-config]
    [httpd.crate.mod-gnutls :as gnutls]
    [httpd.crate.mod-jk :as jk]
    [httpd.crate.mod-rewrite :as rewrite]
    [httpd.crate.webserver-maintainance :as maintainance]
    [org.domaindrivenarchitecture.pallet.crate.httpd.schema :as schema]))

(s/defn install
  [config :- schema/HttpdConfig]
  (apache2/install-apache2-action)
  (apache2/install-apachetop-action)
  (gnutls/install-mod-gnutls)
  (rewrite/install-mod-rewrite)
  (when (contains? config :jk-configuration)
    (jk/install-mod-jk 
      :jkStripSession (-> config :jk-configuration :jkStripSession)
      :jkWatchdogInterval (-> config :jk-configuration :jkWatchdogInterval)))
  )

(s/defn configure
  [config :- schema/HttpdConfig]
  
  (apache2/config-apache2-production-grade)
  
  (apache2/configure-file-and-enable 
    "limits.conf" 
    (httpd-config/limits 
      :max-clients (get-in config [:limits :max-clients])
      :server-limit (get-in config [:limits :server-limit])))
  )
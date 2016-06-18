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
    [httpd.crate.apache2 :as apache2]
    [httpd.crate.config :as httpd-config]
    [httpd.crate.mod-gnutls :as gnutls]
    [httpd.crate.mod-jk :as jk]
    [httpd.crate.mod-rewrite :as rewrite]
    [httpd.crate.webserver-maintainance :as maintainance]
    [org.domaindrivenarchitecture.pallet.crate.httpd.schema :as schema]))

(s/defn reducer-module-used :- s/Bool
  "searches throug the whole config in oder to find out, wheter a specific module is used."
  [key :- s/Keyword]
  (fn [vhost-config1 vhost-config2]
    (or (contains? vhost-config1 key)
        (contains? vhost-config2 key))))

(s/defn module-used? :- s/Bool
  "searches throug the whole config in oder to find out, wheter a specific module is used."
  [config :- schema/HttpdConfig
   key :- s/Keyword]
  (reduce (reducer-module-used key)  (get-in config [:vhosts])))

(s/defn install
  [config :- schema/HttpdConfig]
  (apache2/install-apache2-action)
  (apache2/install-apachetop-action)
  (gnutls/install-mod-gnutls)
  ;TODO: here should only be jk-install not the creation of a remote file
  (when (module-used? config :mod-jk)
    (jk/install-mod-jk :jkStripSession (get-in config [:mod-jk :jkStripSession])
                       :jkWatchdogInterval (get-in config [:mod-jk :jkWatchdogInterval])))
  (rewrite/install-mod-rewrite))

(s/defn configure
  [config :- schema/HttpdConfig]
  (let [vhost-config (first (get-in config [:vhosts]))]
    (apache2/config-apache2-production-grade)
    (maintainance/write-maintainance-file 
      :content (get-in vhost-config [:maintainance-page-content]))
  ))
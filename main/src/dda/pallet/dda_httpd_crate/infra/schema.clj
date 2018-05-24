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
(ns dda.pallet.dda-httpd-crate.infra.schema
  (:require
    [schema.core :as s]
    [dda.pallet.dda-httpd-crate.infra.vhost :as vhost]))

(def VhostConfig vhost/VhostConfig)

(def mod-jk-configuration vhost/ModJkConfiguration)

(def jk-configuration
  "Defines the schema for a jk-configuration, not mod-jk!"
  {:jkStripSession s/Str
   :jkWatchdogInterval s/Int})

(def HttpdConfig
  {:apache-version (s/enum "2.2" "2.4")
   :vhosts vhost/AllVhostConfig
   (s/optional-key :jk-configuration) jk-configuration
   (s/optional-key :limits) {(s/optional-key :server-limit) s/Int
                             (s/optional-key :max-clients) s/Int}
   ;TODO: review gec 2016_12_28: Maybe add also a possibility to disable modules
   (s/optional-key :apache-modules) {(s/optional-key :a2enmod) [s/Str]
                                     (s/optional-key :install) [s/Str]}
   (s/optional-key :settings) (hash-set (s/enum :name-based))})

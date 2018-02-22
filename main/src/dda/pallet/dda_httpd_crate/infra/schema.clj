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
    [schema.core :as s]))

(def mod-jk-configuration
  {:tomcat-forwarding-configuration
   {:mount [{:path s/Str :worker s/Str}]
    (s/optional-key :unmount) [{:path s/Str :worker s/Str}]}
   :worker-properties [{:worker s/Str
                        :host s/Str
                        :port s/Str
                        :maintain-timout-sec s/Int
                        :socket-connect-timeout-ms s/Int}]})

(def VhostConfig
  "defines a schema for a httpdConfig"
  {:domain-name s/Str
   :listening-port s/Str
   :server-admin-email s/Str
   (s/optional-key :server-aliases) [s/Str]
   (s/optional-key :access-control) [s/Str]
   (s/optional-key :document-root) s/Str
   (s/optional-key :rewrite-rules) [s/Str]
   (s/optional-key :user-credentials) [s/Str]
   (s/optional-key :alias) [{:url s/Str :path s/Str}]
   (s/optional-key :alias-match) [{:regex s/Str :path s/Str}]
   (s/optional-key :location) {(s/optional-key :basic-auth) s/Bool
                               (s/optional-key :locations-override) [s/Str]
                               (s/optional-key :path) s/Str}
   ; either letsencrypt or manual certificates
   (s/optional-key :cert-letsencrypt) {:domains [s/Str]
     ; TODO: apply rename refactoring:letsencrypt-mail -> email
                                       :email s/Str}
   (s/optional-key :cert-manual) {:domain-cert s/Str
                                  :domain-key s/Str
                                  (s/optional-key :ca-cert) s/Str}
   (s/optional-key :cert-file) {:domain-cert s/Str
                                :domain-key s/Str
                                (s/optional-key :ca-cert) s/Str}
   ; mod_jk
   (s/optional-key :mod-jk) mod-jk-configuration
   ;proxy
   (s/optional-key :proxy) {:target-port s/Str
                            :additional-directives [s/Str]}

   ; other stuff
   (s/optional-key :maintainance-page-content) [s/Str]
   (s/optional-key :maintainance-page-worker) s/Str
   (s/optional-key :google-id) s/Str
   (s/optional-key :google-worker) s/Str})


(def jk-configuration
  "Defines the schema for a jk-configuration, not mod-jk!"
  {:jkStripSession s/Str
   :jkWatchdogInterval s/Int})

(def HttpdConfig
  {:apache-version (s/enum "2.2" "2.4")
   :vhosts {s/Keyword VhostConfig}
   (s/optional-key :jk-configuration) jk-configuration
   (s/optional-key :limits) {(s/optional-key :server-limit) s/Int
                             (s/optional-key :max-clients) s/Int}
   ;TODO: review gec 2016_12_28: Maybe add also a possibility to disable modules
   (s/optional-key :apache-modules) {(s/optional-key :a2enmod) [s/Str]
                                     (s/optional-key :install) [s/Str]}
   (s/optional-key :settings) (hash-set (s/enum :name-based))})

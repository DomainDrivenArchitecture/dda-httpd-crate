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

(ns org.domaindrivenarchitecture.pallet.crate.httpd.schema
  (:require
    [schema.core :as s]))

; TODO: krj 2016.05.27: should consider if/where :domain-name needs to be inserted
;see vhost/prefix-wrapper and vhost/configure for details
(def VhostConfig
  "defines a schema for a httpdConfig"
  {:domain-name s/Str
   :listening-port s/Str 
   :server-admin-email s/Str
   ; either letsencrypt or manual certificates
   (s/optional-key :cert-letsencrypt) {:letsencrypt-mail s/Str} 
   (s/optional-key :cert-manual) {:domain-cert s/Str 
                                  :domain-key s/Str 
                                  (s/optional-key :ca-cert) s/Str}
   ; mod_jk
   ; TODO review jem 2016_06_14: sub map entries should not be optional. 
   (s/optional-key :mod-jk) {(s/optional-key :app-port) s/Str
                             (s/optional-key :host) s/Str
                             (s/optional-key :worker) s/Str
                             (s/optional-key :socket-timeout) s/Int
                             (s/optional-key :socket-connect-timeout) s/Int
                             (s/optional-key :JkStripSession) s/Str
                             (s/optional-key :JkWatchdogInterval) s/Int
                             }
   ;limits
   (s/optional-key :limits) {(s/optional-key :server-limit) s/Int
                             (s/optional-key :max-clients) s/Int}
   ; other stuff
   (s/optional-key :maintainance-page-content) [s/Str]
   (s/optional-key :google-id) s/Str})

(def HttpdConfig 
  {:vhosts {s/Keyword VhostConfig}})

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

(def HttpdConfig
  "defines a schema for a httpdConfig"
  {:fqdn s/Str
   :listening-port s/Str 
   :server-admin-email s/Str
   ; TODO: not shure, wheter this works as expected - pls add test. Structure should be 
   ; ... 
   ;:server-admin-email "" 
   ;:letsencrypt true 
   ;:letsencrypt-mail ...
   :httpd
   (s/conditional
     #(= (:letsencrypt %) true)
     {:letsencrypt (s/eq true) 
      :letsencrypt-mail s/Str}
     #(= (:letsencrypt %) false)
     {:letsencrypt (s/eq false) 
      :domain-cert s/Str 
      :domain-key s/Str 
      (s/optional-key :ca-cert) s/Str})
   :consider-jk s/Bool
   (s/optional-key :maintainance-page-content) [s/Str]
   (s/optional-key :app-port) s/Str
   (s/optional-key :google-id) s/Str})


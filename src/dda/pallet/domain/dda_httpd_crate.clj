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
(ns dda.pallet.domain.dda-httpd-crate
  (:require
    [schema.core :as s]
    [dda.pallet.core.dda-crate :as dda-crate]
    [dda.pallet.crate.dda-httpd-crate :as httpd-crate]
    [dda.pallet.domain.dda-httpd-crate.schema :as domain-schema]
    [dda.pallet.domain.dda-httpd-crate.static-webserver :as static-webserver]))

(defn static-webserver-stack-configuration
  [domain-config
   & {:keys [group-key] :or {group-key :dda-httpd-group}}]
  (static-webserver/crate-stack-configuration domain-config :group-key group-key))

(def with-httpd
 (dda-crate/create-server-spec httpd-crate/httpd-crate))

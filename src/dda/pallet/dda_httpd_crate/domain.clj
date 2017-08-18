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
(ns dda.pallet.dda-httpd-crate.domain
  (:require
    [schema.core :as s]
    [dda.pallet.core.dda-crate :as dda-crate]
    [dda.pallet.dda-httpd-crate.infra :as infra]
    [dda.pallet.dda-httpd-crate.domain.schema :as domain-schema]
    [dda.pallet.dda-httpd-crate.domain.compatibility-domain :as compat]
    [dda.pallet.dda-httpd-crate.domain.single-static :as single-static]
    [dda.pallet.dda-httpd-crate.domain.multi-static :as multi-static]))

(def SingleStaticConfig domain-schema/SingleStaticConfig)

(def MultiStaticConfig domain-schema/MultiStaticConfig)

(def CompatibilityConfig compat/HttpdDomainConfig)

(s/defn ^:allways-validate compat-configuration
  [domain-config :- CompatibilityConfig]
  {infra/facility
    (compat/crate-configuration domain-config)})

(s/defn ^:allways-validate single-static-configuration
  [domain-config :- SingleStaticConfig]
  {infra/facility
    (single-static/infra-configuration domain-config)})

(s/defn ^:allways-validate multi-static-configuration
  [domain-config :- MultiStaticConfig]
  {infra/facility
    (multi-static/infra-configuration domain-config)})

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
    [dda.pallet.dda-httpd-crate.infra :as infra]
    [dda.pallet.dda-httpd-crate.domain.schema :as domain-schema]
    [dda.pallet.dda-httpd-crate.domain.compatibility-domain :as compat]
    [dda.pallet.dda-httpd-crate.domain.single-static :as single-static]
    [dda.pallet.dda-httpd-crate.domain.jk :as jk]
    [dda.pallet.dda-httpd-crate.domain.multi-static :as multi-static]
    [dda.pallet.dda-httpd-crate.domain.tomcat :as tomcat]))

; ----------------------- schemas --------------------------
(def SingleStaticConfig single-static/SingleStaticConfig)

(def MultiStaticConfig multi-static/MultiStaticConfig)

(def JkConfig domain-schema/JkConfig)

(def CompatibilityConfig compat/HttpdDomainConfig)

(def TomcatConfig domain-schema/TomcatConfig)

(def HttpdDomainConfig
  (s/either
    SingleStaticConfig
    MultiStaticConfig
    JkConfig
    CompatibilityConfig
    TomcatConfig))

; ------- functions to create configs from specific domain configs -----
(s/defn ^:always-validate compat-configuration
  [domain-config :- CompatibilityConfig]
  {infra/facility
    (compat/crate-configuration domain-config)})

(s/defn ^:always-validate single-static-configuration
  [domain-config :- SingleStaticConfig]
  {infra/facility
    (single-static/infra-configuration domain-config)})

(s/defn ^:always-validate multi-static-configuration
  [domain-config :- MultiStaticConfig]
  {infra/facility
    (multi-static/infra-configuration domain-config)})

(s/defn ^:always-validate jk-configuration
  [domain-config :- JkConfig]
  {infra/facility
    (jk/infra-configuration domain-config)})

(s/defn ^:always-validate tomcat-configuration
  [domain-config :- TomcatConfig]
  {infra/facility
    (tomcat/infra-configuration domain-config)})

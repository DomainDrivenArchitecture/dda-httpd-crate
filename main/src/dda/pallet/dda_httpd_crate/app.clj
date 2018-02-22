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
; limitations under the License.(ns dda.pallet.dda-httpd-crate.app

(ns dda.pallet.dda-httpd-crate.app
  (:require
   [schema.core :as s]
   [pallet.api :as api]
   [dda.pallet.commons.existing :as existing]
   [dda.pallet.commons.external-config :as ext-config]
   [dda.pallet.core.dda-crate :as dda-crate]
   [dda.pallet.dda-config-crate.infra :as config-crate]
   [dda.pallet.commons.secret :as secret]
   [dda.pallet.dda-httpd-crate.infra :as infra]
   [dda.pallet.dda-httpd-crate.domain :as domain]))

(def InfraResult infra/InfraResult)

(def Targets existing/Targets)

(def TargetsResolved existing/TargetsResolved)

(def ProvisioningUser existing/ProvisioningUser)

(def HttpdDomainConfig
  (s/either
    domain/SingleStaticConfig
    domain/MultiStaticConfig
    domain/JkConfig
    domain/CompatibilityConfig
    domain/TomcatConfig))
    

(def HttpdAppConfig
 {:group-specific-config
  {s/Keyword InfraResult}})

(s/defn ^:always-validate
  load-targets :- TargetsResolved
  [file-name :- s/Str]
  (existing/load-targets file-name))

(s/defn ^:always-validate load-domain :- HttpdDomainConfig
  [file-name :- s/Str]
  (ext-config/parse-config file-name))

(s/defn ^:always-validate create-app-configuration :- HttpdAppConfig
  [config :- infra/InfraResult
   group-key :- s/Keyword]
  {:group-specific-config
     {group-key config}})

(def with-httpd infra/with-httpd)

(defn multi-app-configuration
  [domain-config
   & {:keys [group-key] :or {group-key :dda-httpd-group}}]
  (s/validate domain/MultiStaticConfig domain-config)
  (create-app-configuration
   (domain/multi-static-configuration domain-config) group-key))

(defn single-app-configuration
  [domain-config
   & {:keys [group-key] :or {group-key :dda-httpd-group}}]
  (s/validate domain/SingleStaticConfig domain-config)
  (create-app-configuration
   (domain/single-static-configuration domain-config) group-key))

(s/defn ^:always-validate jk-app-configuration :- HttpdAppConfig
  [domain-config :- domain/JkConfig
   & options]
  (let [{:keys [group-key] :or {group-key :dda-httpd-group}} options]
    {:group-specific-config
       {group-key
        (domain/jk-configuration domain-config)}}))

(defn compatibility-app-configuration
  [domain-config
   & {:keys [group-key] :or {group-key :dda-httpd-group}}]
  (s/validate domain/CompatibilityConfig domain-config)
  (create-app-configuration
   (domain/compat-configuration domain-config) group-key))

(s/defn ^:always-validate dda-httpd-group-spec
   [app-config :- HttpdAppConfig]
   (let [group-name (name (key (first (:group-specific-config app-config))))]
     (api/group-spec
      group-name
      :extends [(config-crate/with-config app-config)
                with-httpd])))

; ------ returns a HttpdAppConfig which provides support for tomcat -----
(s/defn ^:always-validate tomcat-app-configuration :- HttpdAppConfig
  [domain-config :- domain/TomcatConfig
   & options]
  (let [{:keys [group-key] :or {group-key :dda-httpd-group}} options]
    {:group-specific-config
       {group-key
        (domain/tomcat-configuration domain-config)}}))

(defn app-configuration
  [domain-config]
  (cond
    (= nil (s/check domain/SingleStaticConfig domain-config)) (single-app-configuration domain-config)
    (= nil (s/check domain/MultiStaticConfig domain-config)) (multi-app-configuration domain-config)
    (= nil (s/check domain/JkConfig domain-config)) (jk-app-configuration domain-config)
    (= nil (s/check domain/CompatibilityConfig domain-config)) (compatibility-app-configuration domain-config)
    (= nil (s/check domain/TomcatConfig domain-config)) (tomcat-app-configuration domain-config)
    :else (s/validate HttpdDomainConfig domain-config)))

(s/defn ^:always-validate existing-provisioning-spec
  "Creates an integrated group spec from a domain config and a provisioning user."
  [domain-config :- HttpdDomainConfig
   provisioning-user :- ProvisioningUser]
  (merge
   (dda-httpd-group-spec (app-configuration domain-config))
   (existing/node-spec (secret/resolve-secrets provisioning-user ProvisioningUser))))

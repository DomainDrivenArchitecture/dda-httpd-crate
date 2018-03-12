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
(ns dda.pallet.dda-httpd-crate.app
  (:require
   [schema.core :as s]
   [pallet.api :as api]
   [dda.cm.group :as group]
   [dda.pallet.core.app :as core-app]
   [dda.pallet.dda-config-crate.infra :as config-crate]
   [dda.pallet.dda-httpd-crate.infra :as infra]
   [dda.pallet.dda-httpd-crate.domain :as domain]))

(def with-httpd infra/with-httpd)

(def InfraResult infra/InfraResult)

(def HttpdDomainConfig domain/HttpdDomainConfig)

(def HttpdAppConfig
 {:group-specific-config
  {s/Keyword InfraResult}})

(defn- create-app-configuration
  [config group-key]
  {:group-specific-config {group-key config}})

(def with-httpd infra/with-httpd)

(s/defn ^:always-validate
  multi-app-configuration :- HttpdAppConfig
  [domain-config :- HttpdDomainConfig
   & options]
  (let [{:keys [group-key]
         :or  {group-key infra/facility}} options]
    (create-app-configuration
     (domain/multi-static-configuration domain-config) group-key)))

(s/defn ^:always-validate
  single-app-configuration :- HttpdAppConfig
  [domain-config :- HttpdDomainConfig
   & options]
  (let [{:keys [group-key]
         :or  {group-key infra/facility}} options]
    (create-app-configuration
     (domain/single-static-configuration domain-config) group-key)))

(s/defn ^:always-validate
  jk-app-configuration :- HttpdAppConfig
  [domain-config :- HttpdDomainConfig
   & options]
  (let [{:keys [group-key]
         :or  {group-key infra/facility}} options]
    (create-app-configuration
      (domain/jk-configuration domain-config) group-key)))

(s/defn ^:always-validate
  tomcat-app-configuration :- HttpdAppConfig
  [domain-config :- HttpdDomainConfig
   & options]
  (let [{:keys [group-key]
         :or  {group-key infra/facility}} options]
    (create-app-configuration
        (domain/tomcat-configuration domain-config) group-key)))

(s/defn ^:always-validate
  compatibility-app-configuration :- HttpdAppConfig
  [domain-config :- HttpdDomainConfig
   & options]
  (let [{:keys [group-key]
         :or  {group-key infra/facility}} options]
    (create-app-configuration
     (domain/compat-configuration domain-config) group-key)))

(s/defn ^:always-validate
  app-configuration :- HttpdAppConfig
  [domain-config :- HttpdDomainConfig
   & options]
  (let [{:keys [group-key]
         :or  {group-key infra/facility}} options
        switch (first (keys domain-config))]
    (cond
      (= switch :single-static) (single-app-configuration
                                  domain-config :group-key group-key)
      (= switch :multi-static) (multi-app-configuration
                                  domain-config :group-key group-key)
      (= switch :jk) (jk-app-configuration
                                  domain-config :group-key group-key)
      (= switch :tomcat) (tomcat-app-configuration
                                  domain-config :group-key group-key)
      (= switch :compat) (compatibility-app-configuration
                                  domain-config :group-key group-key))))

(s/defmethod ^:always-validate
  core-app/group-spec infra/facility
  [crate-app
   domain-config :- HttpdDomainConfig]
  (let [app-config (app-configuration domain-config)]
    (group/group-spec
      app-config [(config-crate/with-config app-config)
                  with-httpd])))

(def crate-app (core-app/make-dda-crate-app
                  :facility infra/facility
                  :domain-schema HttpdDomainConfig
                  :domain-schema-resolved HttpdDomainConfig
                  :default-domain-file "httpd.edn"))

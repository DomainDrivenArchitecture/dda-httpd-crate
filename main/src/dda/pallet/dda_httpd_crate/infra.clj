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
(ns dda.pallet.dda-httpd-crate.infra
  (:require
   [schema.core :as s]
   [pallet.actions :as actions]
   [dda.pallet.core.infra :as core-infra]
   [dda.pallet.dda-httpd-crate.infra.schema :as httpd-schema]
   [dda.pallet.dda-httpd-crate.infra.letsencrypt :as letsencrypt]
   [dda.pallet.dda-httpd-crate.infra.server :as server]
   [dda.pallet.dda-httpd-crate.infra.vhost :as vhost]))

(def facility :dda-httpd)

(def VhostConfig httpd-schema/VhostConfig)

(def HttpdConfig httpd-schema/HttpdConfig)

(def InfraResult
 {facility HttpdConfig})

(s/defn ^:always-validate install-httpd
  [config :- HttpdConfig]
  (letsencrypt/install-letsencrypt)
  (server/install config)
  (vhost/install config))

(s/defn ^:always-validate configure-httpd
  [config :- HttpdConfig]
  (server/configure config)
  (vhost/configure config))

(defmethod core-infra/dda-init facility
  [core-infra config]
  (actions/package-manager :update))

(defmethod core-infra/dda-install facility
  [core-infra config]
  (install-httpd config))

(defmethod core-infra/dda-configure facility
  [core-infra config]
  (configure-httpd config))

(def httpd-crate
  (core-infra/make-dda-crate-infra
   :facility facility))

(def with-httpd
  (core-infra/create-infra-plan httpd-crate))

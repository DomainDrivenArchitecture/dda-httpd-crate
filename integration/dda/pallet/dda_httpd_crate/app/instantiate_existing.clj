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

(ns dda.pallet.dda-httpd-crate.app.instantiate-existing
  (:require
    [clojure.inspector :as inspector]
    [pallet.repl :as pr]
    [pallet.actions :as pa]
    [dda.pallet.commons.session-tools :as session-tools]
    [dda.pallet.commons.pallet-schema :as ps]
    [dda.pallet.commons.operation :as operation]
    [dda.pallet.commons.existing :as existing]
    [dda.pallet.dda-httpd-crate.app :as app]))

(def provisioning-ip
  "78.47.55.114")

(def provisioning-user
  {:login "root"})

(def single-config {:domain-name "test1.meissa-gmbh.de"
                    :settings #{:test}})

(def multi-config {:test1.meissa-gmbh.de {:settings #{:test}}
                   :test2.meissa-gmbh.de {:settings #{:test}}})

(def domain-config-compatibility
  {:vhosts
   {:example-vhost
    {:domain-name "my-domain"}}})

(defn provider []
  (existing/provider provisioning-ip "node-id" "dda-httpd-group"))

(defn integrated-group-spec []
  (merge
    (app/dda-httpd-group (app/multi-app-configuration multi-config))
    (existing/node-spec provisioning-user)))

(defn apply-install []
  (pa/set-force-overwrite true)
  (pr/session-summary
    (operation/do-apply-install (provider) (integrated-group-spec))))

(defn apply-config []
  (pa/set-force-overwrite true)
  (pr/session-summary
    (operation/do-apply-configure (provider) (integrated-group-spec))))

(defn server-test []
  (pr/session-summary
    (operation/do-server-test (provider) (integrated-group-spec))))

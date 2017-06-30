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

(ns dda.pallet.crate.dda-httpd-crate.instantiate-existing
  (:require
    [clojure.inspector :as inspector]
    [org.domaindrivenarchitecture.pallet.commons.session-tools :as session-tools]
    [org.domaindrivenarchitecture.pallet.commons.pallet-schema :as ps]
    [dda.cm.operation :as operation]
    [dda.cm.existing :as existing]
    [dda.pallet.crate.dda-httpd-crate.group :as group]
    [dda.pallet.domain.dda-httpd-crate :as domain]
    [dda.pallet.domain.dda-httpd-crate.static-webserver :as sw-domain]
    [dda.pallet.domain.dda-httpd-crate.compatibility-domain :as compatibility-domain]))

(def provisioning-ip
  "52.29.77.123")

(def provisioning-user
  {:login "ubuntu"})

(def domain-config {:domain-name "aws.meissa-gmbh.de"
                    :settings #{}})

(def domain-config-compatibility
  {:vhosts
   {:example-vhost
    {:domain-name "my-domain"}}})

(defn provider []
  (existing/provider provisioning-ip "node-id" "dda-httpd-group"))

(defn integrated-group-spec []
  (merge
    (group/dda-httpd-group (sw-domain/crate-stack-configuration domain-config))
    (existing/node-spec provisioning-user)))

(defn apply-install []
  (operation/do-apply-install (provider) (integrated-group-spec)))

(defn apply-config []
  (operation/do-apply-configure (provider) (integrated-group-spec)))

(defn server-test []
  (operation/do-server-test (provider) (integrated-group-spec)))

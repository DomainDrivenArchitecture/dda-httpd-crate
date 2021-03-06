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
(ns dda.pallet.dda-httpd-crate.domain.compatibility-domain-test-2-1
  {:deprecated "2.1"}
  (:require
   [clojure.test :refer :all]
   [schema.core :as s]
   [dda.pallet.dda-httpd-crate.domain.compatibility-domain-2-1 :as sut]))

(def pair-vhost-test-convention-config-1
  {:input-config {:domain-name "domain.name"}
   :expected {:domain-name "domain.name"
              :listening-port "443"
              :server-admin-email "admin@localdomain"
              :access-control ["Order allow,deny" "Allow from all" ""]}})

(def pair-vhost-test-convention-config-2
  {:input-config {:domain-name "domain.name"
                  :listening-port "420"
                  :access-control ["Override" "Access" "Control"]}
   :expected {:domain-name "domain.name"
              :listening-port "420"
              :access-control ["Override" "Access" "Control"]
              :server-admin-email "admin@localdomain"}})

(def pair-domain-config-1
  {:input-config {:compat
                  {:vhosts
                   {:example-vhost
                    {:domain-name "my-domain"}}}}
   :expected {:vhosts
              {:example-vhost
               {:domain-name "my-domain",
                :listening-port "443",
                :server-admin-email "admin@localdomain",
                :access-control
                ["Order allow,deny" "Allow from all" ""]}},
              :apache-version "2.4",
              :jk-configuration {:jkStripSession "On", :jkWatchdogInterval 120},
              :limits {:server-limit 150, :max-clients 150}}})

(deftest vhost-test
  (s/set-fn-validation! true)
  (is (= (:expected pair-vhost-test-convention-config-1)
         (sut/create-vhost-stack-config-from-domain
          (:input-config pair-vhost-test-convention-config-1))))
  (is (= (:expected pair-vhost-test-convention-config-2)
         (sut/create-vhost-stack-config-from-domain
          (:input-config pair-vhost-test-convention-config-2)))))

(deftest crate-config-test
  (s/set-fn-validation! true)
  (is (= (:expected pair-domain-config-1)
         (sut/crate-configuration
          (:input-config pair-domain-config-1)))))

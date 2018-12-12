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
(ns dda.pallet.dda-httpd-crate.domain.single-proxy-test
  (:require
   [clojure.test :refer :all]
   [schema.core :as s]
   [dda.pallet.dda-httpd-crate.domain.single-proxy :as sut]))

(def pair1
  {:input {:single-proxy {:domain-name "test.domaindrivenarchitecture.org"
                          :proxy-target-port "1234"
                          :settings #{:without-maintainance}}}
   :expected {:apache-version "2.4",
              :limits {:server-limit 150, :max-clients 150},
              :settings #{:name-based},
              :vhosts
              {:default
               {:domain-name "test.domaindrivenarchitecture.org",
                :listening-port "443",
                :server-admin-email "admin@domaindrivenarchitecture.org",
                :document-root "/var/www/test.domaindrivenarchitecture.org",
                :proxy {:target-port "1234"
                        :additional-directives ["ProxyPreserveHost On"
                                                "ProxyRequests     Off"]}
                :cert-letsencrypt {:email "admin@domaindrivenarchitecture.org",
                                   :domains ["test.domaindrivenarchitecture.org"]},}}}
   :settings #{:name-based}})

(def pair2
  {:input {:single-proxy {:domain-name "test.domaindrivenarchitecture.org"
                          :settings #{:without-maintainance}}}
   :expected
   {:apache-version "2.4",
    :limits {:server-limit 150, :max-clients 150}
    :vhosts {:default
                {:domain-name "test.domaindrivenarchitecture.org",
                 :listening-port "443",
                 :server-admin-email "admin@domaindrivenarchitecture.org",
                 :document-root "/var/www/test.domaindrivenarchitecture.org",
                 :proxy {:target-port "8080"
                         :additional-directives ["ProxyPreserveHost On"
                                                 "ProxyRequests     Off"]}
                 :cert-letsencrypt {:email "admin@domaindrivenarchitecture.org",
                                    :domains ["test.domaindrivenarchitecture.org"]},}}
    :settings #{:name-based}}})

(deftest config-test
  (s/set-fn-validation! true)
  (testing
    ; (is (= (:expected pair1)
    ;        (sut/infra-configuration (:input pair1))))

    (is (= (:expected pair2)
           (sut/infra-configuration (:input pair2))))))

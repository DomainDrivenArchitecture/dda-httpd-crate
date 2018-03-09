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
(ns dda.pallet.dda-httpd-crate.domain.single-static-test
  (:require
   [clojure.test :refer :all]
   [schema.core :as s]
   [dda.pallet.dda-httpd-crate.domain.single-static :as sut]))

(def pair1
  {:input {:single-static {:domain-name "test.domaindrivenarchitecture.org"}}
   :expected {}})

(def pair2
  {:input {:single-static {:domain-name "test.domaindrivenarchitecture.org"
                           :settings #{:without-maintainance :with-php}}}
   :expected
   {:apache-version "2.4",
    :limits {:server-limit 150, :max-clients 150}
    :vhosts {:default
                {:domain-name "test.domaindrivenarchitecture.org",
                 :listening-port "443",
                 :server-admin-email "admin@domaindrivenarchitecture.org",
                 :document-root "/var/www/test.domaindrivenarchitecture.org",
                 :location {:locations-override ["Options FollowSymLinks"
                                                 "AllowOverride All"]}
                 :cert-letsencrypt {:email "admin@domaindrivenarchitecture.org",
                                    :domains ["test.domaindrivenarchitecture.org"]},}}
    :settings #{:name-based},
    :apache-modules {:a2enmod ["php7.0"], :install ["libapache2-mod-php7.0"]}}})

(deftest config-test
  (s/set-fn-validation! true)
  (testing
    (is (sut/infra-configuration (:input pair1)))

    (is (= (:expected pair2)
           (sut/infra-configuration (:input pair2))))))

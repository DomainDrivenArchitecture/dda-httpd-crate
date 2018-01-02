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

(ns dda.pallet.dda-httpd-crate.domain.tomcat-test
  (:require
   [clojure.test :refer :all]
   [dda.pallet.dda-httpd-crate.domain.tomcat :as sut]))

(def tomcat-infra-config
 {:apache-version "2.4",
  :limits {:server-limit 150, :max-clients 150}
  :vhosts {:default
              {:domain-name "test.domaindrivenarchitecture.org",
               :listening-port "443",
               :server-admin-email "admin@domaindrivenarchitecture.org",
               :document-root "/var/www/test.domaindrivenarchitecture.org",
               :cert-letsencrypt {:email "admin@domaindrivenarchitecture.org",
                                  :domains ["test.domaindrivenarchitecture.org"]},
               :mod-jk {:tomcat-forwarding-configuration {:mount [{:path "jkpath" :worker "wrkr1"} {:path "/*" :worker "mod_jk_www"}]
                                                          :unmount [{:path "jkpath" :worker "wrkr1"}{:path "jkpath" :worker "wrkr2"}{:path "/error/*" :worker "mod_jk_www"}]}
                        :worker-properties [{:worker "mod_jk_www"
                                             :host "localhost"
                                             :port "8009"
                                             :maintain-timout-sec 90
                                             :socket-connect-timeout-ms 62000}]}
               :alias [{:url "alias-url", :path "alias-path"}]}}
  :settings #{:name-based},
  :jk-configuration
    {:jkStripSession "On"
     :jkWatchdogInterval 120}})

(def domain-test-config
  {:domain-name "test.domaindrivenarchitecture.org"
   :alias [{:url "alias-url" :path "alias-path"}]
   :jk-mount [{:path "jkpath" :worker "wrkr1"}]
   :jk-unmount [{:path "jkpath" :worker "wrkr1"}{:path "jkpath" :worker "wrkr2"}]
   :settings #{:without-maintainance}})

(deftest config-test
  (testing
    (is (sut/infra-configuration
         {:domain-name "test.domaindrivenarchitecture.org"}))
    (is (=
         tomcat-infra-config
         (sut/infra-configuration domain-test-config)))))

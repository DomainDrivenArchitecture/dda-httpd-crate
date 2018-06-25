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
   [schema.core :as s]
   [dda.pallet.dda-httpd-crate.domain :as domain-space]
   [dda.pallet.dda-httpd-crate.domain.tomcat :as sut]))


(def pair1
  {:input {:tomcat {:domain-name "test.domaindrivenarchitecture.org"}}
   :expected {:apache-version "2.4",
              :limits {:server-limit 150, :max-clients 150},
              :settings #{:name-based},
              :jk-configuration
              {:jkStripSession "On", :jkWatchdogInterval 120},
              :vhosts
              {:default
               {:domain-name "test.domaindrivenarchitecture.org",
                :listening-port "443",
                :document-root
                "/var/www/test.domaindrivenarchitecture.org",
                :server-admin-email "admin@domaindrivenarchitecture.org",
                :mod-jk
                {:tomcat-forwarding-configuration
                 {:mount [{:path "/*", :worker "mod_jk_www"}]},
                 :worker-properties
                 [{:worker "mod_jk_www",
                   :host "localhost",
                   :port "8009",
                   :maintain-timout-sec 90,
                   :socket-connect-timeout-ms 62000}]},
                :maintainance-page-content
                ["<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">"

                 "<html>"
                 "<head>"
                 "<title>test.domaindrivenarchitecture.org maintainance</title>"
                 "<meta name=\"ROBOTS\" content=\"NOINDEX, NOFOLLOW\">"
                 "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">"
                 "<meta http-equiv=\"content-type\" content=\"application/xhtml+xml; charset=UTF-8\">"
                 "<meta http-equiv=\"content-style-type\" content=\"text/css\">"
                 "<meta http-equiv=\"expires\" content=\"0\">"
                 "  <style type=\"text/css\">"
                 "    * {background-color: #EEF0F2}"
                 "  </style>"
                 "</head>"
                 "<body>"
                 "  <center>"
                 "    <h1>Maintainance ongoing</h1>"
                 "    <h2>At the moment we're down due to do some maintainance. Please retry in a view moments.</h2>"
                 "  </center>"
                 "</body>"
                 "</html>"],
                :cert-letsencrypt
                {:domains ["test.domaindrivenarchitecture.org"],
                 :email "admin@domaindrivenarchitecture.org"}}}}})

(def pair2
  {:input {:tomcat {:domain-name "test.domaindrivenarchitecture.org"
                    :alias [{:url "alias-url" :path "alias-path"}]
                    :jk-mount [{:path "jkpath" :worker "wrkr1"}]
                    :jk-unmount [{:path "jkpath" :worker "wrkr1"}{:path "jkpath" :worker "wrkr2"}]
                    :settings #{:without-maintainance}}}
   :expected
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
                                                              :unmount [{:path "jkpath" :worker "wrkr1"}{:path "jkpath" :worker "wrkr2"}]}
                            :worker-properties [{:worker "mod_jk_www"
                                                 :host "localhost"
                                                 :port "8009"
                                                 :maintain-timout-sec 90
                                                 :socket-connect-timeout-ms 62000}]}
                   :alias [{:url "alias-url", :path "alias-path"}]}}
      :settings #{:name-based},
      :jk-configuration
        {:jkStripSession "On"
         :jkWatchdogInterval 120}}})

(def domain-test-config {:tomcat {:domain-name "example.de"
                                  :settings #{:test}
                                  :alias [{:url "/quiz/"
                                           :path "/var/www/static/quiz/"}]
                                  :jk-unmount [{:path "/quiz/*" :worker "mod_jk_www"}]}})

(deftest config-test
  (s/set-fn-validation! true)
  (testing
    (is (s/validate domain-space/HttpdDomainConfig domain-test-config))
    (is (s/validate domain-space/HttpdDomainConfig (:input pair2)))
    (is (=
         (:expected pair1)
         (sut/infra-configuration (:input pair1))))
    (is (=
         (:expected pair2)
         (sut/infra-configuration (:input pair2))))))

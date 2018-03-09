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
(ns dda.pallet.dda-httpd-crate.domain.multi--static-test
  (:require
   [clojure.test :refer :all]
   [schema.core :as s]
   [dda.pallet.dda-httpd-crate.domain.multi-static :as sut]))

(def pair1
  {:input {:multi-static {:test.domaindrivenarchitecture.org {}}}
   :expected
   {:apache-version "2.4",
    :limits {:server-limit 150, :max-clients 150},
    :settings #{:name-based},
    :vhosts
    {:test.domaindrivenarchitecture.org
     {:domain-name "test.domaindrivenarchitecture.org",
      :listening-port "443",
      :document-root
      "/var/www/test.domaindrivenarchitecture.org",
      :server-admin-email "admin@domaindrivenarchitecture.org",
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
  {:input {:multi-static {:test1.domaindrivenarchitecture.org
                          {:settings #{:without-maintainance}}
                          :test2.domaindrivenarchitecture.org
                          {:settings #{:without-maintainance}}}}
   :expected
   {:apache-version "2.4",
    :limits {:server-limit 150, :max-clients 150},
    :settings #{:name-based},
    :vhosts
    {:test1.domaindrivenarchitecture.org
     {:domain-name "test1.domaindrivenarchitecture.org",
      :listening-port "443",
      :document-root
      "/var/www/test1.domaindrivenarchitecture.org",
      :server-admin-email "admin@domaindrivenarchitecture.org",
      :cert-letsencrypt
      {:domains ["test1.domaindrivenarchitecture.org"],
       :email "admin@domaindrivenarchitecture.org"}},
     :test2.domaindrivenarchitecture.org
     {:domain-name "test2.domaindrivenarchitecture.org",
      :listening-port "443",
      :document-root
      "/var/www/test2.domaindrivenarchitecture.org",
      :server-admin-email "admin@domaindrivenarchitecture.org",
      :cert-letsencrypt
      {:domains ["test2.domaindrivenarchitecture.org"],
       :email "admin@domaindrivenarchitecture.org"}}}}})

(deftest config-test
  (s/set-fn-validation! true)
  (testing
    (is (= (:expected pair1)
           (sut/infra-configuration (:input pair1))))

    (is (= (:expected pair2)
           (sut/infra-configuration (:input pair2))))))

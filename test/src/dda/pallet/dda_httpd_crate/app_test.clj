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

(ns dda.pallet.dda-httpd-crate.app-test
  (:require
    [clojure.test :refer :all]
    [schema.core :as s]
    [dda.pallet.dda-httpd-crate.app :as sut]))


(def partial-config
  {})

(deftest server-spec
  (testing
    "test the server spec"
    (is
     (contains?
      (:group-specific-config
        (sut/multi-app-configuration
          {:multi-static {:test1.domaindrivenarchitecture.org
                          {:settings #{:without-maintainance}}
                          :test2.domaindrivenarchitecture.org
                          {:settings #{:without-maintainance}}}}
         :group-key :test-key))
      :test-key))))

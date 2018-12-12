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
(ns dda.pallet.dda-httpd-crate.domain.domain-name
  (:require
    [clojure.string :as st]
    [schema.core :as s]))

(defn root-domain? [domain-name]
  (<= (count (st/split domain-name #"\."))
      2))

(defn calculate-domains [domain-name]
  (if (root-domain? domain-name)
    [domain-name (str "www." domain-name)]
    [domain-name]))

(defn calculate-root-domain [domain-name]
  (let [parts (st/split domain-name #"\.")
        length (count parts)]
    (str (nth parts (- length 2)) "." (nth parts (- length 1)))))

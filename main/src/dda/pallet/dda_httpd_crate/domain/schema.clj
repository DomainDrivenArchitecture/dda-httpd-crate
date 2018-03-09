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
(ns dda.pallet.dda-httpd-crate.domain.schema
  (:require
    [schema.core :as s]))

(def VhostConfig
  {(s/optional-key :google-id) s/Str
   (s/optional-key :settings)
   (hash-set (s/enum :test
                     :without-maintainance
                     :with-php))})

(def SingleStaticConfig
  {:single-static
   (merge
     {:domain-name s/Str
      (s/optional-key :alias) [{:url s/Str :path s/Str}]
      (s/optional-key :alias-match) [{:regex s/Str :path s/Str}]}
     VhostConfig)})

(def MultiStaticConfig
  {:multi-static
   {s/Keyword VhostConfig}})

(def JkConfig
  {:jk
   (merge
     VhostConfig
     {:domain-name s/Str
      (s/optional-key :settings)
      (hash-set (s/enum :test
                        :without-maintainance))})})

(def TomcatConfig
  {:tomcat
   (merge
     VhostConfig
     {:domain-name s/Str
      (s/optional-key :alias) [{:url s/Str :path s/Str}]
      (s/optional-key :jk-mount) [{:path s/Str :worker s/Str}]
      (s/optional-key :jk-unmount) [{:path s/Str :worker s/Str}]
      (s/optional-key :settings)
      (hash-set (s/enum :test
                        :without-maintainance))})})

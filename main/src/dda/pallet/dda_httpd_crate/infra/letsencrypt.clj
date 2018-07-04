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

(ns dda.pallet.dda-httpd-crate.infra.letsencrypt
  (:require
    [schema.core :as s]
    [pallet.actions :as actions]))

(defn install-letsencrypt
  "installs letsencrypt package."
  []
  (actions/package "python-letsencrypt-apache"))

(s/defn install-letsencrypt-certs
  "installs letsencrypt certificate."
  [domains :- [s/Str]
   email :- s/Str]
  (println domains)
  (println email)
  (let [domains-param (apply str (interpose " "
                                   (map (fn
                                          [d]
                                          (str " -d " d)) domains)))]
    (actions/exec-script
      ("/etc/init.d/apache2" "stop"))
    (actions/exec-script
        ("letsencrypt" "certonly" "--standalone"
                       "-w " "/var/www/html"
                       "--agree-tos" "--non-interactive"
                       ~domains-param
                       "--email" ~email))
    (actions/exec-script
      ("/etc/init.d/apache2" "start"))))

(s/defn renew-letsencrypt-cron-lines
  "add cron job running at 1:?? AM."
  []
  ["54 1 * * * root /usr/bin/letsencrypt renew --webroot -w /var/www/html --non-interactive"])

(defn configure-renew-cron
  "write renew script."
  []
  (actions/remote-file
    "/etc/cron.d/letsencrypt-renew"
    :mode "0644"
    :overwrite-changes true
    :literal true
    :content (clojure.string/join
               \newline
               (renew-letsencrypt-cron-lines))))

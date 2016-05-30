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

(ns org.domaindrivenarchitecture.pallet.crate.httpd.vhost-test
  (:require
    [clojure.test :refer :all]
    [schema.core :as s]
    [org.domaindrivenarchitecture.pallet.core.dda-crate :as dda-crate]
    [org.domaindrivenarchitecture.pallet.crate.httpd :as httpd]
    [org.domaindrivenarchitecture.pallet.crate.httpd.vhost :as sut]
  ))

(def liferay-config 
  {:fqdn "intermediate.intra.politaktiv.org"
   :server-admin-email "admin@politaktiv.org"
   :cert-letsencrypt {:letsencrypt-mail "admin@politaktiv.org"}
   :google-id "ggl1234"
   })

(def liferay-example-vhost
  ["<VirtualHost *:443>"
  "  ServerName intermediate.intra.politaktiv.org"
  "  ServerAdmin admin@politaktiv.org"
  ""  
  "  <Location />"
  "    Order deny,allow"
  "    Deny from all"
  "    Satisfy any"
  ""    
  "    AuthName \"Authorization for intermediate.intra.politaktiv.org\""
  "    AuthType Basic"
  "    AuthBasicProvider file"
  "    AuthUserFile /etc/apache2/htpasswd-intermediate.intra.politaktiv.org"
  "    Require valid-user"
  "  </Location>"
  ""  
  "  JkMount /* mod_jk_www"
  ""  
  "  Alias /ggl1234.html \"/var/www/static/google/ggl1234.html\""
  "  JkUnMount /ggl1234.html mod_jk_www"
  ""  
  "  ErrorDocument 503 /error/503.html"
  "  Alias /error \"/var/www/static/error\""
  "  JkUnMount /error/* mod_jk_www"
  ""  
  "  ErrorLog \"/var/log/apache2/error.log\""
  "  LogLevel warn"
  "  CustomLog \"/var/log/apache2/ssl-access.log\" combined"
  ""
  "  GnuTLSEnable on"
  "  GnuTLSCacheTimeout 300"
  "  GnuTLSPriorities SECURE:!VERS-SSL3.0:!MD5:!DHE-RSA:!DHE-DSS:!AES-256-CBC:%COMPAT"
  "  GnuTLSExportCertificates on"
  ""  
  "  GnuTLSCertificateFile /etc/letsencrypt/live/intermediate.intra.politaktiv.org/fullchain.pem"
  "  GnuTLSKeyFile /etc/letsencrypt/live/intermediate.intra.politaktiv.org/privkey.pem"
  ""  
  "</VirtualHost>"])

(deftest vhost
  (testing 
    "test the server spec" 
    (is (= liferay-example-vhost
          (sut/vhost 
            (dda-crate/merge-config httpd/dda-httpd-crate liferay-config)))
    )))

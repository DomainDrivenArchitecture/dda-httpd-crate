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

(def vhost-test-config 
  {:domain-name "subdomain.domain.tld"
   :server-admin-email "admin@domain.tld"
   :cert-letsencrypt {:letsencrypt-mail "admin@domain.tld"}
   :google-id "ggl1234"
   :listening-port "443"
   :mod-jk {:app-port "8009"}
   :maintainance-page-content ["test"]
   })

(def vhost-expected
  ["<VirtualHost *:443>"
  "  ServerName subdomain.domain.tld"
  "  ServerAdmin admin@domain.tld"
  "  "  
  "  <Location />"
  "    Order allow,deny"
  "    Allow from all"
  "    "    
  "    AuthName \"Authorization for subdomain.domain.tld\""
  "    AuthType Basic"
  "    AuthBasicProvider file"
  "    AuthUserFile /etc/apache2/htpasswd-subdomain.domain.tld"
  "    Require valid-user"
  "  </Location>"
  "  "  
  "  JkMount /* mod_jk_www"
  "  "
  "  Alias /googleggl1234.html \"/var/www/static/google/googleggl1234.html\""
  "  JkUnMount /googleggl1234.html mod_jk_www"
  "  "  
  "  ErrorDocument 503 /error/503.html"
  "  Alias /error \"/var/www/static/error\""
  "  JkUnMount /error/* mod_jk_www"
  "  "  
  "  ErrorLog \"/var/log/apache2/error.log\""
  "  LogLevel warn"
  "  CustomLog \"/var/log/apache2/ssl-access.log\" combined"
  "  "
  "  GnuTLSEnable on"
  "  GnuTLSCacheTimeout 300"
  "  GnuTLSPriorities SECURE:!VERS-SSL3.0:!MD5:!DHE-RSA:!DHE-DSS:!AES-256-CBC:%COMPAT"
  "  GnuTLSExportCertificates on"
  "  "  
  "  GnuTLSCertificateFile /etc/letsencrypt/live/subdomain.domain.tld/fullchain.pem"
  "  GnuTLSKeyFile /etc/letsencrypt/live/subdomain.domain.tld/privkey.pem"
  "  "  
  "</VirtualHost>"])

(def etc-libapache2-mod-jk-workers-properties
  ["workers.tomcat_home=/usr/share/tomcat6"
   "workers.java_home=/usr/lib/jvm/default-java"
   "ps=/"
   ""
   "#"
   "#------ worker list ------------------------------------------"
   "worker.list=mod_jk_www"
   "worker.mod_jk_www.port=8009"
   "worker.mod_jk_www.host=127.0.0.1"
   "worker.mod_jk_www.type=ajp13"
   "worker.mod_jk_www.socket_timeout=900"
   "worker.mod_jk_www.socket_keepalive=false"
   "worker.mod_jk_www.connection_pool_timeout=100"
   ])

(def etc-libapache2-mod-jk-httpd-jk-conf
  ["<IfModule jk_module>"
   "    JkWorkersFile /etc/libapache2-mod-jk/workers.properties"
   "    JkLogFile /var/log/apache2/mod_jk.log"
   "    JkLogLevel info"
   "    JkShmFile /var/log/apache2/jk-runtime-status"
   "    # JkOptions +RejectUnsafeURI"
   "    # JkStripSession On"
   "    JkWatchdogInterval 60"
   "    <Location /jk-status>"
   "        JkMount jk-status"
   "        Order deny,allow"
   "        Deny from all"
   "        Allow from 127.0.0.1"
   "    </Location>"
   "    <Location /jk-manager>"
   "        JkMount jk-manager"
   "        Order deny,allow"
   "        Deny from all"
   "        Allow from 127.0.0.1"
   "    </Location>"
   "</IfModule>"])

(deftest vhost
  (testing 
    "Test the creation of an example vhost from configuration." 
    (is (= vhost-expected (sut/vhost vhost-test-config)))
    ))

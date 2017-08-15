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

(ns dda.pallet.dda-httpd-crate.infra.vhost-test
  (:require
    [dda.config.commons.string-test-utils :as string-utils]
    [clojure.test :refer :all]
    [schema.core :as s]
    [httpd.crate.mod-jk :as jk]
    [dda.pallet.dda-httpd-crate.infra.vhost :as sut]))


(def vhost-test-config
  {:domain-name "subdomain.domain.tld"
   :server-admin-email "admin@domain.tld"
   :location {:basic-auth true}
   :access-control ["Order allow,deny" "Allow from all" ""]
   :cert-letsencrypt {:letsencrypt-mail "admin@domain.tld"}
   :google-id "ggl1234"
   :listening-port "443"
   :mod-jk {:tomcat-forwarding-configuration {:mount [{:path "/*" :worker "mod_jk_www"}
                                                      {:path "/test" :worker "mod_jk_www_test"}]
                                              :unmount [{:path "/unmount" :worker "mod_jk_www_unmount"}
                                                        {:path "/unmount2" :worker "mod_jk_ww_unmount2"}]}

            :worker-properties [{:worker "mod_jk_www"
                                 :host "127.0.0.1"
                                 :port "8009"
                                 :maintain-timout-sec 90
                                 :socket-connect-timeout-ms 62000}
                                {:worker "mod_jk_www2"
                                 :host "127.0.0.2"
                                 :port "8002"
                                 :maintain-timout-sec 22
                                 :socket-connect-timeout-ms 62002}]}

   :maintainance-page-content ["test"]})


(def vhost-pa-config
  {:domain-name "subdomain.domain.tld"
   :server-admin-email "admin@domain.tld"
   :location {:basic-auth true}
   :access-control ["Order deny,allow" "Deny from all" "Satisfy any"
                    "SetEnvIf Request_URI \"/webdav/*\" noauth" "Allow from env=noauth" ""]
   :alias [{:url "/quiz/" :path "/var/www/static/quiz/"}]
   :mod-jk {:tomcat-forwarding-configuration {:mount [{:path "/*" :worker "mod_jk_www"}
                                                      {:path "/*" :worker "liferay-worker"}]

                                              :unmount [{:path "/quiz/*" :worker "mod_jk_www"}
                                                        {:path "/*" :worker "mod_jk_www"}]}
            :worker-properties
            [{:port "8009" :host "127.0.0.1" :worker "mod_jk_www" :maintain-timout-sec 90 :socket-connect-timeout-ms 60000}
             {:port "8009" :host "127.0.0.1" :worker "liferay-worker" :maintain-timout-sec 900 :socket-connect-timeout-ms 60000}]}

   :cert-letsencrypt {:letsencrypt-mail "admin@domain.tld"}
   :google-id "ggl1234"
   :listening-port "443"
   :maintainance-page-content ["test"]})


(def test-config
  {:limits {:server-limit 150
            :max-clients 150}
   :jk-configuration {:jkStripSession "On"
                      :jkWatchdogInterval 120}
   :vhosts {}})

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
   "  JkMount /test mod_jk_www_test"
   "  JkUnMount /unmount mod_jk_www_unmount"
   "  JkUnMount /unmount2 mod_jk_ww_unmount2"
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
   "  GnuTLSPriorities SECURE:!VERS-SSL3.0:!MD5:!DHE-RSA:!DHE-DSS:!AES-256-CBC:%COMPAT"
   "  GnuTLSExportCertificates on"
   "  "
   "  GnuTLSCertificateFile /etc/letsencrypt/live/subdomain.domain.tld/fullchain.pem"
   "  GnuTLSKeyFile /etc/letsencrypt/live/subdomain.domain.tld/privkey.pem"
   "  "
   "</VirtualHost>"
   " "
   "JkWorkerProperty worker.list=mod_jk_www"
   "JkWorkerProperty worker.maintain=90"
   "JkWorkerProperty worker.mod_jk_www.port=8009"
   "JkWorkerProperty worker.mod_jk_www.host=127.0.0.1"
   "JkWorkerProperty worker.mod_jk_www.type=ajp13"
   "JkWorkerProperty worker.mod_jk_www.socket_connect_timeout=62000"
   "JkWorkerProperty worker.mod_jk_www.ping_mode=I"
   "JkWorkerProperty worker.mod_jk_www.socket_keepalive=true"
   "JkWorkerProperty worker.mod_jk_www.connection_pool_timeout=100"
   ""
   "JkWorkerProperty worker.list=mod_jk_www2"
   "JkWorkerProperty worker.maintain=22"
   "JkWorkerProperty worker.mod_jk_www2.port=8002"
   "JkWorkerProperty worker.mod_jk_www2.host=127.0.0.2"
   "JkWorkerProperty worker.mod_jk_www2.type=ajp13"
   "JkWorkerProperty worker.mod_jk_www2.socket_connect_timeout=62002"
   "JkWorkerProperty worker.mod_jk_www2.ping_mode=I"
   "JkWorkerProperty worker.mod_jk_www2.socket_keepalive=true"
   "JkWorkerProperty worker.mod_jk_www2.connection_pool_timeout=100"])

(def pa-vhost
  ["<VirtualHost *:443>"
   "ServerName subdomain.domain.tld"
   "ServerAdmin admin@domain.tld"
   ""
   "<Location />"
   "Order deny,allow"
   "Deny from all"
   "Satisfy any"
   "SetEnvIf Request_URI \"/webdav/*\" noauth"
   "Allow from env=noauth"
   ""
   "AuthName \"Authorization for subdomain.domain.tld\""
   "AuthType Basic"
   "AuthBasicProvider file"
   "AuthUserFile /etc/apache2/htpasswd-subdomain.domain.tld"
   "    Require valid-user"
   "</Location>"
   ""
   "Alias \"/quiz/\" \"/var/www/static/quiz/\""
   ""
   "JkMount /* mod_jk_www"
   "JkMount /* liferay-worker"
   "JkUnMount /quiz/* mod_jk_www"
   "JkUnMount /* mod_jk_www"
   ""
   "  Alias /googleggl1234.html \"/var/www/static/google/googleggl1234.html\""
   "JkUnMount /googleggl1234.html mod_jk_www"
   ""
   "  ErrorDocument 503 /error/503.html"
   "  Alias /error \"/var/www/static/error\""
   "  JkUnMount /error/* mod_jk_www"
   ""
   "  ErrorLog \"/var/log/apache2/error.log\""
   "  LogLevel warn"
   "  CustomLog \"/var/log/apache2/ssl-access.log\" combined"
   "  "
   "  GnuTLSEnable on"
   "  GnuTLSPriorities SECURE:!VERS-SSL3.0:!MD5:!DHE-RSA:!DHE-DSS:!AES-256-CBC:%COMPAT"
   "  GnuTLSExportCertificates on"
   "  "
   "  GnuTLSCertificateFile /etc/letsencrypt/live/subdomain.domain.tld/fullchain.pem"
   "  GnuTLSKeyFile /etc/letsencrypt/live/subdomain.domain.tld/privkey.pem"
   "</VirtualHost>"
   "JkWorkerProperty worker.list=mod_jk_www"
   "JkWorkerProperty worker.maintain=90"
   "JkWorkerProperty worker.mod_jk_www.port=8009"
   "JkWorkerProperty worker.mod_jk_www.host=127.0.0.1"
   "JkWorkerProperty worker.mod_jk_www.type=ajp13"
   "JkWorkerProperty worker.mod_jk_www.socket_connect_timeout=60000"
   "JkWorkerProperty worker.mod_jk_www.ping_mode=I"
   "JkWorkerProperty worker.mod_jk_www.socket_keepalive=true"
   "JkWorkerProperty worker.mod_jk_www.connection_pool_timeout=100"
   "JkWorkerProperty worker.list=liferay-worker"
   "JkWorkerProperty worker.maintain=900"
   "JkWorkerProperty worker.liferay-worker.port=8009"
   "JkWorkerProperty worker.liferay-worker.host=127.0.0.1"
   "JkWorkerProperty worker.liferay-worker.type=ajp13"
   "JkWorkerProperty worker.liferay-worker.socket_connect_timeout=60000"
   "JkWorkerProperty worker.liferay-worker.ping_mode=I"
   "JkWorkerProperty worker.liferay-worker.socket_keepalive=true"
   "JkWorkerProperty worker.liferay-worker.connection_pool_timeout=100"])

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
   "worker.mod_jk_www.connection_pool_timeout=100"])

(deftest vhost
  (testing
    "Test the creation of an example vhost from configuration."
    (is (= (string-utils/trim-string-vector vhost-expected) (string-utils/trim-string-vector (sut/vhost vhost-test-config))))
    (is (= (string-utils/trim-string-vector pa-vhost) (string-utils/trim-string-vector (sut/vhost vhost-pa-config))))))

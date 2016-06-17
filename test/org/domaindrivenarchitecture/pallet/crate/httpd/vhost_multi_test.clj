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

(ns org.domaindrivenarchitecture.pallet.crate.httpd.vhost-multi-test
  (:require
    [clojure.test :refer :all]
    [schema.core :as s]
    [org.domaindrivenarchitecture.pallet.core.dda-crate :as dda-crate]
    [org.domaindrivenarchitecture.pallet.crate.httpd :as httpd]
    [org.domaindrivenarchitecture.pallet.crate.httpd.vhost :as sut]
    [httpd.crate.vhost :as vhost]
  ))

(def VhostConfig2
  "defines a schema for a httpdConfig"
  {:domain-name s/Str
   :listening-port s/Str 
   :server-admin-email s/Str
   ; either letsencrypt or manual certificates
   (s/optional-key :cert-letsencrypt) {:letsencrypt-mail s/Str} 
   (s/optional-key :cert-manual) {:domain-cert s/Str 
                                  :domain-key s/Str 
                                  (s/optional-key :ca-cert) s/Str}
   ; mod_jk
   ; TODO review jem 2016_06_14: sub map entries should not be optional. 
   (s/optional-key :mod-jk) {(s/optional-key :app-port) s/Str
                             (s/optional-key :host) s/Str
                             (s/optional-key :worker) s/Str
                             (s/optional-key :socket-timeout) s/Int
                             (s/optional-key :socket-connect-timeout) s/Int
                             (s/optional-key :JkStripSession) s/Str
                             (s/optional-key :JkWatchdogInterval) s/Int
                             }
   ;limits
   (s/optional-key :limits) {(s/optional-key :server-limit) s/Int
                             (s/optional-key :max-clients) s/Int}
   ; other stuff
   (s/optional-key :maintainance-page-content) [s/Str]
   (s/optional-key :google-id) s/Str})

(def etc-apache2-meissa-config
  {:domain-name "jira.meissa-gmbh.de"
   :listening-port "80"
   :server-admin-email "admin@jira.meissa-gmbh.de"})

(def etc-apache2-politaktiv-config
  {:domain-name "jira.politaktiv.org"
   :listening-port "80"
   :server-admin-email "admin@jira.politaktiv.org"})

; This equals (vhost/vhost-conf-default-redirect-to-https-only :domain-name (get-in config1 [:domain-name]) :server-admin-email (get-in config1 [:server-admin-email]))
(def etc-apache2-sites-enabled-000-meissa-conf
  ["<VirtualHost *:80>"
  "  ServerName jira.meissa-gmbh.de"
  "  ServerAdmin admin@jira.meissa-gmbh.de"
  "  "
  "  ErrorLog \"/var/log/apache2/error.log\""
  "  LogLevel warn"
  "  CustomLog \"/var/log/apache2/access.log\" combined"
  "  "
  "  RewriteEngine on"
  "  RewriteCond %{HTTPS} !on"
  "  RewriteRule ^/(.*)$ https://%{SERVER_NAME}/$1 [R=301,L]"
  "  "
  "</VirtualHost>"])

(def etc-apache2-sites-enabled-000-politaktiv-conf
  ["<VirtualHost *:80>"
   "  ServerName jira.politaktiv.org"
   "  ServerAdmin admin@jira.politaktiv.org"
   "  "
   "  ErrorLog \"/var/log/apache2/error.log\""
   "  LogLevel warn"
   "  CustomLog \"/var/log/apache2/access.log\" combined"
   "  "
   "  RewriteEngine on"
   "  RewriteCond %{HTTPS} !on"
   "  RewriteRule ^/(.*)$ https://%{SERVER_NAME}/$1 [R=301,L]"
   "  "
   "</VirtualHost>"])

(def etc-apache2-sites-enabled-000-meissa-ssl-conf
  ["<VirtualHost *:443>"
   "  ServerName jira.meissa-gmbh.de"
   "  ServerAdmin admin@jira.meissa-gmbh.de"
   "  "
   "  ProxyPreserveHost On"
   "  ProxyRequests     Off"
   "  ProxyPass / http://localhost:8080/"
   "  ProxyPassReverse / http://localhost:8080/"
   "  ErrorLog \"/var/log/apache2/error.log\""
   "  LogLevel warn"
   "  CustomLog \"/var/log/apache2/ssl-access.log\" combined"
   "  "
   "  GnuTLSEnable on"
   "  GnuTLSCacheTimeout 300"
   "  GnuTLSPriorities SECURE:!VERS-SSL3.0:!MD5:!DHE-RSA:!DHE-DSS:!AES-256-CBC:%COMPAT"
   "  GnuTLSExportCertificates on"
   "  "
   "  #GnuTLSCertificateFile /etc/apache2/ssl.crt/jira.meissa-gmbh.de.certs"
   "  #GnuTLSKeyFile /etc/apache2/ssl.key/jira.meissa-gmbh.de.key"
   "  GnuTLSCertificateFile  /etc/letsencrypt/live/jira.meissa-gmbh.de/cert.pem"
   "  GnuTLSKeyFile /etc/letsencrypt/live/jira.meissa-gmbh.de/privkey.pem"
   "  GnuTLSClientCAFile /etc/letsencrypt/live/jira.meissa-gmbh.de/fullchain.pem"
   "  "
   "</VirtualHost>"])

(def etc-apache2-sites-enabled-000-politaktiv-ssl-conf
  ["<VirtualHost *:443>"
   "  ServerName jira.politaktiv.org"
   "  ServerAdmin admin@jira.politaktiv.org"
   "  "
   "  ProxyPreserveHost On"
   "  ProxyRequests     Off"
   "  ProxyPass / http://localhost:8180/"
   "  ProxyPassReverse / http://localhost:8180/"
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
   "  GnuTLSCertificateFile /etc/apache2/ssl.crt/jira.politaktiv.org.certs"
   "  GnuTLSKeyFile /etc/apache2/ssl.key/jira.politaktiv.org.key"
   "  "
   "</VirtualHost>"])

(deftest vhost
  (testing 
    "Test the creation of an example vhost from configuration." 
    (is (= etc-apache2-sites-enabled-000-meissa-conf
          (vhost/vhost-conf-default-redirect-to-https-only 
            :domain-name (get-in etc-apache2-meissa-config [:domain-name]) 
            :server-admin-email (get-in etc-apache2-meissa-config [:server-admin-email]))))
    (is (= etc-apache2-sites-enabled-000-politaktiv-conf
          (vhost/vhost-conf-default-redirect-to-https-only 
            :domain-name (get-in etc-apache2-politaktiv-config [:domain-name]) 
            :server-admin-email (get-in etc-apache2-politaktiv-config [:server-admin-email]))))
    ))

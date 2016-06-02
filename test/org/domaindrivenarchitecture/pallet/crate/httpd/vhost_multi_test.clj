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
  ))

(def etc-apache2-sites-enabled-000-meissa.conf
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

(def etc-apache2-sites-enabled-000-politaktiv.conf
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

(def etc-apache2-sites-enabled-000-meissa-ssl.conf
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

(def etc-apache2-sites-enabled-000-politaktiv-ssl.conf
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

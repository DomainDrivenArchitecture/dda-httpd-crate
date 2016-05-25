(ns org.domaindrivenarchitecture.pallet.crate.httpd
  (:require
    [schema.core :as s]
     [schema-tools.core :as st]
  ))


(def httpdConfig
  "defines a schema for a httpdConfig"
  {:letsencrypt s/Bool
   :fqdn s/Str
   :app-port s/Str
   :maintainance-page-content [s/Str]})




(def httpd-webserver-configuration
{; Webserver Configuration
 :letsencrypt true
 :fqdn "localhost.localdomain"
 :app-port "8009"
 :maintainance-page-content ["<h1>Webserver Maintainance Mode</h1>"]})



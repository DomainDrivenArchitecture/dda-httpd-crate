; TODO: review jem: 2016_05_25: copyright comment is missing.

(ns org.domaindrivenarchitecture.pallet.crate.httpd
  (:require
    [schema.core :as s]
     [schema-tools.core :as st]
  ))

; TODO: review jem: 2016_05_25: Naming convention is HttpdConfig
(def httpdConfig
  "defines a schema for a httpdConfig"
  {:fqdn s/Str
   :app-port s/Str
   :maintainance-page-content [s/Str]
   ; review jem: 2016_05_25: parts from liferay was missing!   
   (s/conditional
       #(= (:letsencrypt %) true)
       {:letsencrypt (s/eq true) 
        :letsencrypt-mail s/Str
        :fqdn s/Str
        (s/optional-key :app-port) s/Str
        (s/optional-key :google-id) s/Str
        (s/optional-key :maintainance-page-content) [s/Str]}
       #(= (:letsencrypt %) false)
       {:letsencrypt (s/eq false) 
        :domain-cert s/Str 
        :domain-key s/Str 
        (s/optional-key :ca-cert) s/Str
        :fqdn s/Str
        (s/optional-key :app-port) s/Str
        (s/optional-key :google-id) s/Str
        (s/optional-key :maintainance-page-content) [s/Str]})
   })

; TODO: review jem: 2016_05_25: name should be default- ??
(def httpd-webserver-configuration
  {; Webserver Configuration
   :letsencrypt true
   :fqdn "localhost.localdomain"
   :app-port "8009"
   :maintainance-page-content ["<h1>Webserver Maintainance Mode</h1>"]})

; TODO: review jem: 2016_05_25: we will need some merge-config function.
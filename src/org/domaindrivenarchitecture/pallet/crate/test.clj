(ns org.domaindrivenarchitecture.pallet.crate.test
  (:require
    [schema.core :as s]
    [schema-tools.core :as st]
    [org.domaindrivenarchitecture.config.commons.map-utils :as map-utils]
    [httpd.crate.apache2 :as apache2]
    [httpd.crate.vhost :as vhost]
    [httpd.crate.config :as httpd-config]
    [httpd.crate.basic-auth :as auth]
    [httpd.crate.mod-gnutls :as gnutls]
    [httpd.crate.mod-jk :as jk]
    [httpd.crate.google-siteownership-verification :as google]
    [httpd.crate.common :as httpd-common]
    [httpd.crate.mod-rewrite :as rewrite]
    [httpd.crate.webserver-maintainance :as maintainance]
  ))

(defn test
  [& {:keys [testus]}]
  (test2 testus)
  )

(defn test2
  [& {:keys [testus] :or {testus 2}} ]
  (println testus))


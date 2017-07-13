(ns dda.pallet.dda-httpd-crate.infra
  (:require
   [schema.core :as s]
   [pallet.actions :as actions]
   [dda.pallet.core.dda-crate :as dda-crate]
   [dda.pallet.dda-httpd-crate.infra.schema :as httpd-schema]
   [dda.pallet.dda-httpd-crate.infra.server :as server]
   [dda.pallet.dda-httpd-crate.infra.vhost :as vhost]))


(def facility :dda-httpd)
(def version [0 1 5])

(def HttpdConfig
  httpd-schema/HttpdConfig)

(s/defn ^:always-validate install-httpd
  [config :- HttpdConfig]
  (server/install config))

(s/defn ^:always-validate configure-httpd
  [config :- HttpdConfig]
  (server/configure config)
  (vhost/configure config))

(defmethod dda-crate/dda-init facility
  [dda-crate config]
  (actions/package-manager :update))

(defmethod dda-crate/dda-install facility
  [dda-crate config]
  (install-httpd config))

(defmethod dda-crate/dda-configure facility
  [dda-crate config]
  (server/configure config)
  (vhost/configure config))

(def httpd-crate
  (dda-crate/make-dda-crate
   :facility facility
   :version version))

(def with-httpd
  (dda-crate/create-server-spec httpd-crate))

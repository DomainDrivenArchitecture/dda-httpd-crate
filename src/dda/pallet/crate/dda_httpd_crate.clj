(ns dda.pallet.crate.dda-httpd-crate
  (:require [dda.pallet.core.dda-crate :as dda-crate]
            [pallet.actions :as actions]
            [dda.pallet.crate.dda-httpd-crate.schema :as httpd-schema]
            [dda.pallet.crate.dda-httpd-crate.server :as server]
            [dda.pallet.crate.dda-httpd-crate.vhost :as vhost]
            [schema.core :as s]))

(def facility :dda-httpd)
(def version [0 1 5])

(def HttpdCrateConfig
  httpd-schema/HttpdConfig)

(s/defn ^:always-validate install-httpd
  [config :- HttpdCrateConfig]
  (server/install config))

(s/defn ^:always-validate configure-httpd
  [config :- HttpdCrateConfig]
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

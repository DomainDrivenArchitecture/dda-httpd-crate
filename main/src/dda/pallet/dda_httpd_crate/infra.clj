(ns dda.pallet.dda-httpd-crate.infra
  (:require
   [schema.core :as s]
   [pallet.actions :as actions]
   [dda.pallet.core.infra :as core-infra]
   [dda.pallet.dda-httpd-crate.infra.schema :as httpd-schema]
   [dda.pallet.dda-httpd-crate.infra.server :as server]
   [dda.pallet.dda-httpd-crate.infra.vhost :as vhost]))


(def facility :dda-httpd)

(def VhostConfig httpd-schema/VhostConfig)

(def HttpdConfig httpd-schema/HttpdConfig)

(def InfraResult
 {facility HttpdConfig})

(s/defn ^:always-validate install-httpd
  [config :- HttpdConfig]
  (server/install config))

(s/defn ^:always-validate configure-httpd
  [config :- HttpdConfig]
  (server/configure config)
  (vhost/configure config))

(defmethod core-infra/dda-init facility
  [core-infra config]
  (actions/package-manager :update))

(defmethod core-infra/dda-install facility
  [core-infra config]
  (install-httpd config))

(defmethod core-infra/dda-configure facility
  [core-infra config]
  (server/configure config)
  (vhost/configure config))

(def httpd-crate
  (core-infra/make-dda-crate-infra
   :facility facility))

(def with-httpd
  (core-infra/create-infra-plan httpd-crate))

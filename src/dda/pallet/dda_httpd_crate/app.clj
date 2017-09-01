(ns dda.pallet.dda-httpd-crate.app
  (:require
   [schema.core :as s]
   [pallet.api :as api]
   [dda.pallet.core.dda-crate :as dda-crate]
   [dda.pallet.dda-config-crate.infra :as config-crate]
   [dda.pallet.dda-httpd-crate.infra :as infra]
   [dda.pallet.dda-httpd-crate.domain :as domain]))

(def InfraResult infra/InfraResult)

(def HttpdAppConfig
 {:group-specific-config
  {s/Keyword InfraResult}})

(s/defn ^:allways-validate create-app-configuration :- HttpdAppConfig
  [config :- infra/HttpdConfig
   group-key :- s/Keyword]
  {:group-specific-config
     {group-key config}})

(def with-httpd infra/with-httpd)

(defn multi-app-configuration
  [domain-config & {:keys [group-key] :or {group-key :dda-httpd-group}}]
  (s/validate domain/MultiStaticConfig domain-config)
  (create-app-configuration
   (domain/multi-static-configuration domain-config) group-key))

(defn single-app-configuration
  [domain-config & {:keys [group-key] :or {group-key :dda-httpd-group}}]
  (s/validate domain/SingleStaticConfig domain-config)
  (create-app-configuration
   (domain/single-static-configuration domain-config) group-key))

(s/defn ^:allways-validate jk-app-configuration :- HttpdAppConfig
  [domain-config :- domain/JkConfig
   & options]
  (let [{:keys [group-key] :or {group-key :dda-httpd-group}} options]
    {:group-specific-config
       {group-key
        (domain/jk-configuration domain-config) group-key}}))

(defn compatibility-app-configuration
  [domain-config & {:keys [group-key] :or {group-key :dda-httpd-group}}]
  (s/validate domain/CompatibilityConfig domain-config)
  (create-app-configuration
   (domain/compat-configuration domain-config) group-key))

(s/defn ^:always-validate dda-httpd-group
   [app-config :- HttpdAppConfig]
   (let [group-name (name (key (first (:group-specific-config app-config))))]
     (api/group-spec
      group-name
      :extends [(config-crate/with-config app-config)
                with-httpd])))

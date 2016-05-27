(ns org.domaindrivenarchitecture.pallet.crate.test
  (:require
    [schema.core :as s]
    [schema-tools.core :as st]
  ))

(defn test2
  [& {:keys [testus] :or {testus 2}} ]
  (println testus))

(defn test
  [& {:keys [testus]}]
  (test2 testus)
  )

(defrecord human
  [name
   age])


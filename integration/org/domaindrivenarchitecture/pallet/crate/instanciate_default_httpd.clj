; TODO: review jem 2016_06_28: license missing. Lets discuss function and location as general pattern in team. 

(ns org.domaindrivenarchitecture.pallet.crate.instanciate-default-httpd
  (:use
      pallet.actions
      pallet.api
      pallet.compute
      pallet.crate.ssh-key
      pallet.script
      pallet.stevedore
      pallet.stevedore.bash)
  (:require
    [org.domaindrivenarchitecture.pallet.crate.httpd :as httpd]
    )
  (:gen-class :main true))

(def my-pocket-cloud
  (pallet.compute/instantiate-provider
    "node-list"
    :node-list [["palletPoc" "poc" "YOUR_IP" :ubuntu]]))

(def pallet-user
  (make-user "YOUR_USERNAME" :password "YOUR_PASSWORD"))

(def group-spec-test
  (server-spec :phases
               {:configure 
                (plan-fn 
                  (httpd/configure httpd/default-config)
                )}))
(def poc
  (group-spec
    "poc"
    :extends [httpd/with-httpd
              :group-spec-test]
    )
  )



(defn -main
  [& args]
  (apply lift
         poc
         :user pallet-user
         :compute my-pocket-cloud
         args))
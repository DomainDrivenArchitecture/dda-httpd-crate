(ns dda.pallet.domain.dda-httpd-crate.compatibility-domain-test
  (:require
   [clojure.test :refer :all]
   [dda.pallet.domain.dda-httpd-crate.compatibility-domain :as sut]))

(def pair-vhost-test-convention-config-1
  {:input-config {:domain-name "domain.name"}
   :expected {:domain-name "domain.name"
              :listening-port "443"
              :server-admin-email "admin@localdomain"
              :access-control ["Order allow,deny" "Allow from all" ""]}})

(def pair-vhost-test-convention-config-2
  {:input-config {:domain-name "domain.name"
                  :listening-port "420"
                  :access-control ["Override" "Access" "Control"]}
   :expected {:domain-name "domain.name"
              :listening-port "420"
              :access-control ["Override" "Access" "Control"]
              :server-admin-email "admin@localdomain"}})

(def domain-config
  {:vhosts
   {:example-vhost
    {:domain-name "my-domain"}}})

(deftest config-test
  (is (= (sut/create-vhost-stack-config-from-domain
          (:input-config pair-vhost-test-convention-config-1))
         (:expected pair-vhost-test-convention-config-1)))
  (is (= (sut/create-vhost-stack-config-from-domain
          (:input-config pair-vhost-test-convention-config-2))
         (:expected pair-vhost-test-convention-config-2))))

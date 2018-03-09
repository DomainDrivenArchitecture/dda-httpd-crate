(ns dda.pallet.dda-httpd-crate.domain.compatibility-domain-test
  (:require
   [clojure.test :refer :all]
   [dda.pallet.dda-httpd-crate.domain.compatibility-domain :as sut]))

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

(def pair-domain-config-1
  {:input-config {:compat
                  {:vhosts
                   {:example-vhost
                    {:domain-name "my-domain"}}}}
   :expected {:vhosts
              {:example-vhost
               {:domain-name "my-domain",
                :listening-port "443",
                :server-admin-email "admin@localdomain",
                :access-control
                ["Order allow,deny" "Allow from all" ""]}},
              :apache-version "2.4",
              :jk-configuration {:jkStripSession "On", :jkWatchdogInterval 120},
              :limits {:server-limit 150, :max-clients 150}}})

(deftest vhost-test
  (is (= (:expected pair-vhost-test-convention-config-1)
         (sut/create-vhost-stack-config-from-domain
          (:input-config pair-vhost-test-convention-config-1))))
  (is (= (:expected pair-vhost-test-convention-config-2)
         (sut/create-vhost-stack-config-from-domain
          (:input-config pair-vhost-test-convention-config-2)))))

(deftest crate-config-test
  (is (= (:expected pair-domain-config-1)
         (sut/crate-configuration
          (:input-config pair-domain-config-1)))))

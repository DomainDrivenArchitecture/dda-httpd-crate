(ns dda.pallet.dda-httpd-crate.domain.single-static-test
  (:require
   [clojure.test :refer :all]
   [dda.pallet.dda-httpd-crate.domain.single-static :as sut]))

(def simple-with-directory
 {:apache-version "2.4",
  :limits {:server-limit 150, :max-clients 150}
  :vhosts {:default
              {:domain-name "test.domaindrivenarchitecture.org",
               :listening-port "443",
               :server-admin-email "admin@domaindrivenarchitecture.org",
               :document-root "/var/www/test.domaindrivenarchitecture.org",
               :location {:locations-override ["Options FollowSymLinks"
                                               "AllowOverride All"]}
               :cert-letsencrypt {:email "admin@domaindrivenarchitecture.org",
                                  :domains ["test.domaindrivenarchitecture.org"]},}}
  :settings #{:name-based},
  :apache-modules {:a2enmod ["mod-php"]}})

(deftest config-test
  (testing
    (is (sut/infra-configuration
         {:domain-name "test.domaindrivenarchitecture.org"}))
    (is (=
         simple-with-directory
         (sut/infra-configuration
          {:domain-name "test.domaindrivenarchitecture.org"
           :settings #{:without-maintainance :with-php}})))))

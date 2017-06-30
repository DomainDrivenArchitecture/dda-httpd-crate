(ns dda.pallet.domain.dda-httpd-crate.static-webserver-test
  (:require
   [clojure.test :refer :all]
   [dda.pallet.domain.dda-httpd-crate.static-webserver :as sut]))

(deftest config-test
  (testing
    (is (sut/crate-stack-configuration
         {:domain-name "test.domaindrivenarchitecture.org"}))))

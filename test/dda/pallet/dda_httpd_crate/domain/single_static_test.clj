(ns dda.pallet.dda-httpd-crate.domain.single-static-test
  (:require
   [clojure.test :refer :all]
   [dda.pallet.dda-httpd-crate.domain.single-static :as sut]))

(deftest config-test
  (testing
    (is (sut/crate-configuration
         {:domain-name "test.domaindrivenarchitecture.org"}))))

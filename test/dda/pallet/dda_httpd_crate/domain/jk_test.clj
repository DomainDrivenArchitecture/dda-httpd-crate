(ns dda.pallet.dda-httpd-crate.domain.jk-test
  (:require
   [clojure.test :refer :all]
   [dda.pallet.dda-httpd-crate.domain.jk :as sut]))

(def jk
 {:apache-version "2.4",
  :limits {:server-limit 150, :max-clients 150}
  :vhosts {:default
              {:domain-name "test.domaindrivenarchitecture.org",
               :listening-port "443",
               :server-admin-email "admin@domaindrivenarchitecture.org",
               :document-root "/var/www/test.domaindrivenarchitecture.org",
               :cert-letsencrypt {:email "admin@domaindrivenarchitecture.org",
                                  :domains ["test.domaindrivenarchitecture.org"]},
               :mod-jk {:tomcat-forwarding-configuration {:mount [{:path "/*" :worker "mod_jk_www"}]
                                                          :unmount [{:path "/error/*" :worker "mod_jk_www"}]}
                        :worker-properties [{:worker "mod_jk_www"
                                             :host "localhost"
                                             :port "8009"
                                             :maintain-timout-sec 90
                                             :socket-connect-timeout-ms 62000}]}}}
  :settings #{:name-based},
  :jk-configuration
    {:jkStripSession "On"
     :jkWatchdogInterval 120}})

(deftest config-test
  (testing
    (is (sut/infra-configuration
         {:domain-name "test.domaindrivenarchitecture.org"}))
    (is (=
         jk
         (sut/infra-configuration
          {:domain-name "test.domaindrivenarchitecture.org"
           :settings #{:without-maintainance}})))))

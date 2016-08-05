(defproject org.domaindrivenarchitecture/dda-httpd-crate "0.1.0-SNAPSHOT"
  :description "new dda-httpd-crate"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.palletops/pallet "0.8.12" :exclusions [org.clojure/tools.cli]]
                 [prismatic/schema "1.1.3"]
                 [org.domaindrivenarchitecture/dda-init-crate "0.1.0-SNAPSHOT"]
                 [org.domaindrivenarchitecture/dda-config-commons "0.1.5"]
                 [org.domaindrivenarchitecture/dda-pallet "0.1.0"]
                 [org.domaindrivenarchitecture/httpd "0.2.3-SNAPSHOT"]]
  :repositories [["snapshots" :clojars]
                 ["releases" :clojars]]
  :deploy-repositories [["snapshots" :clojars]
                        ["releases" :clojars]]
  :profiles {:dev
             {:source-paths ["src" "src"
                             "intergation" "integration"]
              :dependencies
              [[com.palletops/pallet "0.8.12" :classifier "tests"]
               [org.domaindrivenarchitecture/dda-config-commons "0.1.5" :classifier "tests"]
               [org.clojure/test.check "0.9.0"]]
              :plugins
              [[com.palletops/pallet-lein "0.8.0-alpha.1"]
               [lein-sub "0.3.0"]]}
             :leiningen/reply
             {:dependencies [[org.slf4j/jcl-over-slf4j "1.7.21"]]
              :exclusions [commons-logging]}}
  :local-repo-classpath true
  )

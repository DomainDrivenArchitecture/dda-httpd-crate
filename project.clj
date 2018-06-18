(defproject dda/dda-httpd-crate "2.0.7-SNAPSHOT"
  :description "new dda-httpd-crate"
  :url "https://www.domaindrivenarchitecture.org"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[dda/dda-pallet "2.2.0" :exclusions [org.clojure/tools.reader]]
                 [dda/httpd "0.2.9" :exclusions [org.clojure/tools.reader]]]
  :source-paths ["main/src"]
  :resource-paths ["main/resources"]
  :repositories [["snapshots" :clojars]
                 ["releases" :clojars]]
  :deploy-repositories [["snapshots" :clojars]
                        ["releases" :clojars]]
  :profiles {:dev {:source-paths ["integration/src"
                                  "test/src"
                                  "uberjar/src"]
                   :resource-paths ["integration/resources"
                                    "test/resources"]
                   :dependencies
                   [[org.clojure/tools.cli "0.3.7"]
                    [ch.qos.logback/logback-classic "1.2.3"]
                    [org.slf4j/jcl-over-slf4j "1.7.25"]
                    [org.domaindrivenarchitecture/pallet-aws "0.2.8.2"
                                                        :exclusions [com.palletops/pallet]]]
                   :plugins [[lein-sub "0.3.0"]
                             [lein-pprint "1.2.0"]]
                   :leiningen/reply {:dependencies [[org.slf4j/jcl-over-slf4j "1.8.0-beta0"]]
                                     :exclusions [commons-logging]}
                   :repl-options {:init-ns dda.pallet.dda-git-crate.app.instantiate-aws}}
             :test {:test-paths ["test/src"]
                    :resource-paths ["test/resources"]
                    :dependencies [[dda/pallet "0.9.0" :classifier "tests"]]}
             :uberjar {:source-paths ["uberjar/src"]
                       :resource-paths ["uberjar/resources"]
                       :aot :all
                       :main dda.pallet.dda-httpd-crate.main
                       :dependencies [[org.clojure/tools.cli "0.3.7"]
                                      [ch.qos.logback/logback-classic "1.2.3"]
                                      [org.slf4j/jcl-over-slf4j "1.7.25"]]}}
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["uberjar"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]
  :local-repo-classpath true)

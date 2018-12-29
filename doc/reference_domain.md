```clojure
(def HttpdDomainConfig
  (s/either
    SingleStaticConfig
    MultiStaticConfig
    JkConfig
    CompatibilityConfig
    TomcatConfig))

(def SingleStaticValueConfig
    (merge
      {:domain-name s/Str
       (s/optional-key :alias) [{:url s/Str :path s/Str}]
       (s/optional-key :alias-match) [{:regex s/Str :path s/Str}]}
      domain-schema/VhostConfig))

(def MultiStaticConfig
  {:multi-static
   {s/Keyword domain-schema/VhostConfig}})

(def TomcatConfig
 {:tomcat
  (merge
    domain-schema/VhostConfig
    {:domain-name s/Str
     (s/optional-key :alias) [{:url s/Str :path s/Str}]
     (s/optional-key :jk-mount) [{:path s/Str :worker s/Str}]
     (s/optional-key :jk-unmount) [{:path s/Str :worker s/Str}]
     (s/optional-key :settings)
     (hash-set (s/enum :test
                       :without-maintainance))})})

(def CompatibilityConfig
 {:compat
  {(s/optional-key :apache-version) s/Str
   (s/optional-key :jk-configuration) httpd-schema/jk-configuration
   :vhosts {s/Keyword VhostDomainConfig}
   (s/optional-key :limits) {(s/optional-key :server-limit) s/Int
                             (s/optional-key :max-clients) s/Int}
   (s/optional-key :apache-modules) {(s/optional-key :a2enmod)[s/Str]}}})

(def JkConfig
  {:jk
  (merge
    domain-schema/VhostConfig
    {:domain-name s/Str
     (s/optional-key :settings)
     (hash-set (s/enum :test
                       :without-maintainance))})})   

(def SingleStaticConfig
  {:single-static SingleStaticValueConfig})

(def VhostConfig
  {(s/optional-key :google-id) s/Str
   (s/optional-key :settings)
   (hash-set (s/enum :test
                     :without-maintainance
                     :with-php))})
```

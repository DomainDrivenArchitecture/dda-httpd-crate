```clojure
(def HttpdDomainConfig
  (s/either
    SingleStaticConfig
    SingleProxyConfig
    MultiStaticConfig
    JkConfig
    CompatibilityConfig
    TomcatConfig))

;------------------------------- single proxy -----------------------
(def SingleProxyValueConfig
  (merge
    {:domain-name s/Str
     (s/optional-key :proxy-target-port) s/Str}
    generic-vhost/VhostConfig))

(def SingleProxyConfig
  {:single-proxy SingleProxyValueConfig})

;------------------------------- single static -----------------------
(def SingleStaticValueConfig
  (merge
    {:domain-name s/Str}
    generic-vhost/VhostConfig
    {(s/optional-key :settings)
     (hash-set (s/enum :test
                       :without-maintainance
                       :with-php))}))

(def SingleStaticConfig
  {:single-static SingleStaticValueConfig})

;------------------------------- multi static -----------------------
(def MultiStaticConfig
  {:multi-static
   {s/Keyword generic-vhost/VhostConfig}})

;------------------------------- tomcat -----------------------
(def TomcatConfig
  {:tomcat
   (merge
     generic-vhost/VhostConfig
     {:domain-name s/Str
      (s/optional-key :jk-mount) [{:path s/Str :worker s/Str}]
      (s/optional-key :jk-unmount) [{:path s/Str :worker s/Str}]})})

;------------------------------- jk -----------------------
(def JkConfig
  {:jk
   (merge
     generic-vhost/VhostConfig
     {:domain-name s/Str})})

;------------------------------- generic vhost -----------------------
(def generic-vhost/VhostSettings  (hash-set (s/enum :test
                                                    :without-maintainance)))

(def generic-vhost/VhostConfig
  {(s/optional-key :google-id) s/Str
   (s/optional-key :allow-origin) s/Str
   (s/optional-key :alias) [{:url s/Str :path s/Str}]
   (s/optional-key :alias-match) [{:regex s/Str :path s/Str}]
   (s/optional-key :settings) generic-vhost/VhostSettings})
```

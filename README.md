# dda-httpd-crate

[![Clojars Project](https://img.shields.io/clojars/v/dda/dda-httpd-crate.svg)](https://clojars.org/dda/dda-httpd-crate)
[![Build Status](https://travis-ci.org/DomainDrivenArchitecture/dda-httpd-crate.svg?branch=master)](https://travis-ci.org/DomainDrivenArchitecture/dda-httpd-crate)

[![Slack](https://img.shields.io/badge/chat-clojurians-green.svg?style=flat)](https://clojurians.slack.com/messages/#dda-pallet/) | [<img src="https://domaindrivenarchitecture.org/img/meetup.svg" width=50 alt="DevOps Hacking with Clojure Meetup"> DevOps Hacking with Clojure](https://www.meetup.com/de-DE/preview/dda-pallet-DevOps-Hacking-with-Clojure) | [Website & Blog](https://domaindrivenarchitecture.org)

## Jump to
[Features](#features)
[Usage](#usage)
[Details](#details)
[Reference](#reference)
[Targets-config-example](#targets-config-example)
[Dda-httpd-config-example](#Dda-httpd-config-example)
[Targets](#targets)
[Infra-API](#infra-api)
[Compatibility](#compatibility)

## Features
The dda-httpd-crate allows you to specify target-systems and a desired configuration to
* install an apache2 server
* generate vhost configurations
* mod_jk integration
* Generate letsencrypt certificates with automatic renewal
* Forward http requests to https

## Usage
1. **Download the jar-file** from the releases page of this repository (e.g. `curl -L -o httpd.jar https://github.com/DomainDrivenArchitecture/dda-httpd-crate/releases/download/2.0.7/dda-httpd-crate-2.0.7-standalone.jar`)
1. **Create the ```httpd.edn``` configruration** file in the same folder where you saved the jar-file. The ```httpd.edn``` file specifies the configuration used to configure the apache2 server. You may use the following example as a starting point and adjust it according to your own needs:

```clojure
{:single-static {:domain-name "Your Domain Name Here"
                :alias [{:url "/some/url" :path "/some/path"}] ; optionally configure alias
                :alias-match [{:regex "Some REGEX" :path "/some/path"}] ; optionally configure alias-match using regular expressions
                :google-id "Your Google ID" ; optionally configure your Google ID
                :settings #{ ;specify some optional settings as keywords such as
                            :test :without-maintenance :with-php}
  ```
Please note, the keywords marked optional do not have to be specified only if you need them. For documentation on creating regular expressions for alias-match please see the
[apache2 documentation](https://httpd.apache.org/docs/2.4/mod/mod_alias.html).

3. (optional) If you want to install and configure the apache2 server on a remote machine, please create additionally a `targets.edn` file. In this file you define which server(s) the apache2 server should be installed and configured upon. You may use and adjust the following example config:

```clojure
{:existing [{:node-name "target1"                      ; semantic name (keep the default or use a name that suits you)
             :node-ip "192.168.56.104"}]               ; the ip4 address of the machine to be provisioned
             {:node-name "target2"                     ; semantic name (keep the default or use a name that suits you)
                          :node-ip "192.168.56.105"}]  ; the ip4 address of the machine to be provisioned
 :provisioning-user {:login "initial"                  ; user on the target machine, must have sudo rights
                     :password {:plain "secure1234"}}} ; password can be ommited, if a ssh key is authorized
````
5. **Run the jar** with the following options and inspect the output.
  For testing against localhost:
  ```bash
java -jar dda-httpd-crate-standalone.jar httpd.edn
  ```

  For testing remote server(s) please specify the targets file:

  ```bash
java -jar dda-httpd-crate-standalone.jar --targets targets.edn httpd.edn
```

## Additional-info-about-the-configuration
Two configuration files are required by the dda-httpd-crate:: "httpd.edn" and "targets.edn" (or similar names). These files specify both WHAT to install and configure and WHERE, respectively. In detail: the first file defines the configuration for the installation and configuration performed, while the second configuration file specifies the target nodes/systems, on which the installation will be performed. The following examples will explain these files in more detail.

(**Remark:** The second file "targets.edn" is *optional*. This means, if none is specified, then a default file is used, which defines that the installation and configuration is done on the **localhost**.)


### Targets-config-example
```clojure
{:existing [{:node-name "test-vm1"
             :node-ip "35.157.19.218"}
            {:node-name "test-vm2"
             :node-ip "18.194.113.138"}]
 :provisioning-user {:login "ubuntu"}}
```
The keyword ```:existing``` has to be assigned a vector, that contains maps with the information about the nodes.
The nodes are the target machines that apache2 will be configured and installed upon. The ```node-name``` has to be set to be able to identify the target machine and the ```node-ip``` has to be set so that the source machine can reach it.
The ```provisioning-user``` has to be the same for all nodes. Furthermore, if the ssh-key of the executing host is authorized on all target nodes, a password for authorization can be omitted. If this is not the case, the provisioning user has to contain a password.

### Dda-httpd-config-example
```clojure
{:multi-static
 {:test1.meissa-gmbh.de {:settings #{:test}}
  :test2.meissa-gmbh.de {:settings #{:test}}}}

```
The httpd config file defines how the apache2 will be configured. This particula##r config will create a multi-static environment with multiple vhosts and multiple domains. The :test keyword specifies that snakeoil certificates will be used.

## Details
### mod_jk integration
We are configuring our apache httpd server slightly different than what would be expected.
We are defining the worker.properties inside the vhost files because the implementation
inside clojure is much easier.

### Static content rollout

![RolloutStaticContent](/doc/RolloutStaticContent.png)

1. ContentProducer puts content versioned into a m2 repository
2. httpd-crate pulls content from m2
3. and unzips to "/var/www" sub directory


### preconditions for letsencrypt
* vhost contains ServerAlias for all additional names
* dns contains A-Records for all additional names

### Server Maintenance
It should no longer be necessary to renew certifcates manually. Howerver, if this is still necessary for whatever reason to renew a certificate use the following commands on the target server:

* service apache2 stop
* cd /usr/lib/letsencrypt
* ./letsencrypt-auto --standalone renew
* service apache2 start


## Reference
We provide two levels of API - domain is a high level API with many built-in conventions. If these conventions don't fit your needs, you can use our low-level API (infra) and realize your own conventions.

### Domain API

#### Targets
The schema of the domain layer for the targets is:
```clojure
(def ExistingNode
  "Represents a target node with ip and its name."
  {:node-name s/Str   ; semantic name (keep the default or use a name that suits you)
   :node-ip s/Str})   ; the ip4 address of the machine to be provisioned

(def ExistingNodes
  "A sequence of ExistingNodes."
  {s/Keyword [ExistingNode]})

(def ProvisioningUser
  "User used for provisioning."
  {:login s/Str                                ; user on the target machine, must have sudo rights
   (s/optional-key :password) secret/Secret})  ; password can be ommited, if a ssh key is authorized

(def Targets
  "Targets to be used during provisioning."
  {:existing [ExistingNode]                                ; one ore more target nodes.
   (s/optional-key :provisioning-user) ProvisioningUser})  ; user can be ommited to execute on localhost with current user
```
The "targets.edn" file has to match this schema.

#### Dda-httpd
The schema for the httpd configuration is:
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
The "httpd.edn" file has to match this schema. Please note, the either indicates that only one of the options has to be specified as a configuration. If the provided domain configuration options do not fit your needs feel free to use our low level API infra to create your own.

### Infra-API
The infra configuration is a configuration on the infrastructure level of a crate. It contains the complete configuration options that are possible with the crate functions.
On an infra level the dda-http-crate provides all the functions for generating vhosts and other configurations.

The schema is:
```clojure
(def mod-jk-configuration
  {:tomcat-forwarding-configuration
   {:mount [{:path s/Str :worker s/Str}]
    (s/optional-key :unmount) [{:path s/Str :worker s/Str}]}
   :worker-properties [{:worker s/Str
                        :host s/Str
                        :port s/Str
                        :maintain-timout-sec s/Int
                        :socket-connect-timeout-ms s/Int}]})

(def VhostConfig
  "defines a schema for a httpdConfig"
  {:domain-name s/Str
   :listening-port s/Str
   :server-admin-email s/Str
   (s/optional-key :server-aliases) [s/Str]
   (s/optional-key :access-control) [s/Str]
   (s/optional-key :document-root) s/Str
   (s/optional-key :rewrite-rules) [s/Str]
   (s/optional-key :user-credentials) [s/Str]
   (s/optional-key :alias) [{:url s/Str :path s/Str}]
   (s/optional-key :alias-match) [{:regex s/Str :path s/Str}]
   (s/optional-key :location) {(s/optional-key :basic-auth) s/Bool
                               (s/optional-key :locations-override) [s/Str]
                               (s/optional-key :path) s/Str}
   ; either letsencrypt or manual certificates
   (s/optional-key :cert-letsencrypt) {:domains [s/Str]
     ; TODO: apply rename refactoring:letsencrypt-mail -> email
                                       :email s/Str}
   (s/optional-key :cert-manual) {:domain-cert s/Str
                                  :domain-key s/Str
                                  (s/optional-key :ca-cert) s/Str}
   (s/optional-key :cert-file) {:domain-cert s/Str
                                :domain-key s/Str
                                (s/optional-key :ca-cert) s/Str}
   ; mod_jk
   (s/optional-key :mod-jk) mod-jk-configuration
   ;proxy
   (s/optional-key :proxy) {:target-port s/Str
                            :additional-directives [s/Str]}

   ; other stuff
   (s/optional-key :maintainance-page-content) [s/Str]
   (s/optional-key :maintainance-page-worker) s/Str
   (s/optional-key :google-id) s/Str
   (s/optional-key :google-worker) s/Str})

(def jk-configuration
 "Defines the schema for a jk-configuration, not mod-jk!"
 {:jkStripSession s/Str
  :jkWatchdogInterval s/Int})

(def HttpdConfig
  {:apache-version (s/enum "2.2" "2.4")
   :vhosts {s/Keyword VhostConfig}
   (s/optional-key :jk-configuration) jk-configuration
   (s/optional-key :limits) {(s/optional-key :server-limit) s/Int
                             (s/optional-key :max-clients) s/Int}
   (s/optional-key :apache-modules) {(s/optional-key :a2enmod) [s/Str]
                                     (s/optional-key :install) [s/Str]}
   (s/optional-key :settings) (hash-set (s/enum :name-based))})
```


## Compatibility
dda-pallet is compatible with the following versions
 * pallet 0.8
 * clojure 1.7
 * (x)ubunutu 16.0

## License
Published under [apache2.0 license](LICENSE.md)

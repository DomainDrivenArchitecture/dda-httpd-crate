# dda-httpd-crate

[![Clojars Project](https://img.shields.io/clojars/v/dda/dda-httpd-crate.svg)](https://clojars.org/dda/dda-httpd-crate)
[![Build Status](https://travis-ci.org/DomainDrivenArchitecture/dda-httpd-crate.svg?branch=master)](https://travis-ci.org/DomainDrivenArchitecture/dda-httpd-crate)

[![Slack](https://img.shields.io/badge/chat-clojurians-green.svg?style=flat)](https://clojurians.slack.com/messages/#dda-pallet/) | [<img src="https://domaindrivenarchitecture.org/img/meetup.svg" width=50 alt="DevOps Hacking with Clojure Meetup"> DevOps Hacking with Clojure](https://www.meetup.com/de-DE/preview/dda-pallet-DevOps-Hacking-with-Clojure) | [Website & Blog](https://domaindrivenarchitecture.org)

This crate is part of [dda-pallet](https://domaindrivenarchitecture.org/pages/dda-pallet/).

## Jump to
[Compatibility](#compatibility)
[Features](#features)
[Usage](#usage)
[Details](#details)
[Reference](#reference)
[Dda-httpd-config-example](#Dda-httpd-config-example)
[Domain API](#domain-api)
[Infra-API](#infra-api)

## compatability
dda-pallet is compatible to the following versions
* pallet 0.8.x
* clojure 1.9
* (x)ubunutu 18.04

## Features
The dda-httpd-crate allows you to specify target-systems and a desired configuration to
* install an apache2 server
* generate vhost configurations
* mod_jk integration
* Generate letsencrypt certificates with automatic renewal
* Forward http requests to https

## Usage
1. **Download the jar-file** from the releases page of this repository (e.g. `curl -L -o dda-httpd-standalone.jar https://github.com/DomainDrivenArchitecture/dda-httpd-crate/releases/download/2.2.0/dda-httpd-standalone.jar`)
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

### Watch log for debug reasons
In case any problems occur you may want to have a look at the log-file:
'less logs/pallet.log'

## Reference
Some details about the architecture: We provide two levels of API. **Domain** is a high-level API with built-in conventions. If these conventions do not fit your needs you can use our low-level **infra** API and realize your own conventions.

### Targets
You can define provisioning targets using the [targets-schema](https://github.com/DomainDrivenArchitecture/dda-pallet-commons/blob/master/doc/existing_spec.md)

### Domain API
You can use our conventions as a starting point:
[see domain reference](doc/reference_domain.md)

### Infra API
Or you can build your own conventions using our low level infra API. We will keep this API backward compatible whenever possible:
[see infra reference](doc/reference_infra.md)

## License
Copyright © 2015, 2016, 2017, 2018 meissa GmbH
Published under [apache2.0 license](LICENSE)

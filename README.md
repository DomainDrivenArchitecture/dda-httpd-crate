# dda-httpd-crate
## compatability
dda-pallet is compatible to the following versions
 * pallet 0.8.x
 * clojure 1.7
 * (x)ubunutu14.04 / 16.04

## Features
 * http -> https
 * letsencrypt
 * gnutls
 * mod_jk
 * static content rollout

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


## Server Maintenance
To renew a certificate use the following commands on the target server:

* service apache2 stop
* cd /usr/lib/letsencrypt
* ./letsencrypt-auto --standalone renew
* service apache2 start

Please note that you need to be root in order to execute the commands.

# License
Published under [apache2.0 license](LICENSE.md)


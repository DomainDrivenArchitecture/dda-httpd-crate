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
### Static content rollout

![RolloutStaticContent](/doc/RolloutStaticContent.png)

1. ContentProducer puts content versioned into a m2 repository
2. httpd-crate pulls content from m2
3. and unzips to "/var/www" sub directory



# License
Published under [apache2.0 license](LICENSE.md)

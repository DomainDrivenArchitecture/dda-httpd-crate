# dda-httpd-crate

We are configuring our apache httpd server slightly different than what would be expected.
We are defining the worker.properties inside the vhost files because the implementation 
inside clojure is much easier.
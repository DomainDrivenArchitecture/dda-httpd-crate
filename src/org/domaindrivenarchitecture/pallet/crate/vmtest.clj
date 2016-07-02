(ns org.domaindrivenarchitecture.pallet.crate.vmtest
    (:require 
    [org.domaindrivenarchitecture.pallet.core.cli-helper :as cli-helper]
    [org.domaindrivenarchitecture.pallet.crate.config :as config]
    [org.domaindrivenarchitecture.pallet.crate.init :as init]
    [org.domaindrivenarchitecture.pallet.crate.user.ssh-key :as ssh-key-record]
    [pallet.api :as api]
    [clojure.java.io :as io]
    [pallet.compute :as compute]
    [pallet.compute.node-list :as node-list]
    [org.domaindrivenarchitecture.pallet.crate.httpd :as httpd]
    [org.domaindrivenarchitecture.pallet.crate.config.node :as node-record])
  (:gen-class :main true))
  

;(def my-pocket-cloud
  ;(pallet.compute/instantiate-provider
;    "node-list"
;    :node-list [["palletPoc" "poc" "192.168.178.59" :ubuntu]]))

;(def pallet-user
;  (make-user "krj" :password "test1234"))

;
;------------------------------------Thomas----------------------------------
;

(defn dda-read-file 
"reads a file if it exists"
[file-name]
(if (.exists (io/file file-name))
(slurp file-name)
nil))

(def ssh-keys
{:my-key
(ssh-key-record/new-ssh-key
(dda-read-file (str (System/getenv "HOME") "/.ssh/id_rsa.pub"))
(dda-read-file (str (System/getenv "HOME") "/.ssh/id_rsa.pub")))
})

(def os-user
{:root {:authorized-keys [:my-key]}
:pallet {:authorized-keys [:my-key]}
})



(def meissa-vm
(node-record/new-node 
:host-name "my-ide" 
:domain-name "meissa-gmbh.de" 
:pallet-cm-user-name "krj"
:pallet-cm-user-password "test1234"
:additional-config 
{:dda-httpd {}})
)

(def config
{:ssh-keys ssh-keys
:os-user os-user
:node-specific-config {:meissa-vm meissa-vm}
})

(def managed-vm-group
  (api/group-spec
    "managed-vm-group"
    :extends [(config/with-config config)
              init/with-init
              httpd/with-httpd
              ]))

(def localhost-node
  (node-list/make-localhost-node 
    :group-name "managed-vm-group" 
    :id :meissa-vm))

(def remote-node
  (node-list/make-node 
    "meissa-vm" 
    "managed-vm-group" 
    "192.168.178.59"
    :ubuntu
    :id :meissa-vm))

(def node-list
  (compute/instantiate-provider
    "node-list" :node-list [remote-node]))

(defn -main
  "CLI main"
  [& args]
  (apply cli-helper/main 
         :meissa-vm
         managed-vm-group
         node-list 
         config
         args))


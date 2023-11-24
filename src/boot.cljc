(ns boot
  (:require [hyperfiddle.electric :as e]
            user-main))

#?(:clj (defn with-ring-request [ring-req] (e/boot-server user-main/Main ring-req)))
#?(:cljs (def client (e/boot-client user-main/Main nil)))

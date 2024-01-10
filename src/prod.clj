(ns prod
  (:gen-class)
  (:require user-main ; in prod, load app into server so it can accept clients
            [hyperfiddle.electric :as e]
            electric-server-java8-jetty9))

(def electric-server-config
  {:host "0.0.0.0", :port 8080, :resources-path "public"})

(defn -main [& args] ; run with `clj -M -m prod`
  (electric-server-java8-jetty9/start-server!
    (fn [ring-req] (e/boot-server {} user-main/Main ring-req))
    electric-server-config))

; On CLJS side we reuse src/user.cljs for prod entrypoint
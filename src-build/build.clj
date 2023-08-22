(ns build
  "build electric.jar library artifact and demos"
  (:require [clojure.tools.build.api :as b]
            [org.corfield.build :as bb]
            [clojure.java.shell :as sh]))

(def lib 'com.hyperfiddle/electric)
(def version (b/git-process {:git-args "describe --tags --long --always --dirty"}))
(def basis (b/create-basis {:project "deps.edn"}))

(defn clean [opts]
  (bb/clean opts))

(def class-dir "target/classes")
(defn default-jar-name [{:keys [version] :or {version version}}]
  (format "target/%s-%s-standalone.jar" (name lib) version))

(defn clean-cljs [_]
  (b/delete {:path "resources/public/js"}))

(defn build-client [{:keys [optimize debug verbose version]
                     :or {optimize true, debug false, verbose false, version version}}]
  (println "Building client. Version:" version)
  ; this command must run the same on: local machine, docker build, github actions.
  ; shell out to let Shadow manage the classpath, it's not obvious how to set that up.
  ; This is the only stable way we know to do it.
  (let [command (->> ["clj" "-J-Xss4M" "-M:shadow-cljs" "release" "prod"
                      (when debug "--debug")
                      (when verbose "--verbose")
                      "--config-merge"
                      (pr-str {:compiler-options {:optimizations (if optimize :advanced :simple)}
                               :closure-defines  {'hyperfiddle.electric-client/VERSION version}})]
                  (remove nil?))]
    (apply println "Running:" command)
    (let [{:keys [exit out err]} (apply sh/sh command)]
      (when err (println err))
      (when out (println out))
      (when-not (zero? exit)
        (println "Exit code" exit)
        (System/exit exit)))))

(defn uberjar [{:keys [jar-name version optimize debug verbose]
                :or   {version version, optimize true, debug false, verbose false}}]
  (println "Cleaning up before build")
  (clean nil)

  (println "Cleaning cljs compiler output")
  (clean-cljs nil)

  (build-client {:optimize optimize, :debug debug, :verbose verbose, :version version})

  (println "Bundling sources")
  (b/copy-dir {:src-dirs   ["src" "resources"]
               :target-dir class-dir})

  (println "Compiling server. Version:" version)
  (b/compile-clj {:basis      basis
                  :src-dirs   ["src"]
                  :ns-compile '[prod]
                  :class-dir  class-dir})

  (println "Building uberjar")
  (b/uber {:class-dir class-dir
           :uber-file (str (or jar-name (default-jar-name {:version version})))
           :basis     basis
           :main      'prod}))

(defn noop [_])                         ; run to preload mvn deps
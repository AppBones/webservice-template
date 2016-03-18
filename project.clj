(defproject appbone-service-template "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [http-kit "2.1.19"]
                 [ring "1.4.0"]
                 [liberator "0.14.0"]
                 [io.sarnowski/swagger1st "0.21.0"]
                 [com.stuartsierra/component "0.3.1"]]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.10"]
                                  [compojure "1.5.0"]]}
             :prod {:source-paths ["shim"]}})

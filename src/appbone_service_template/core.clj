(ns appbone-service-template.core
  (:require [com.stuartsierra.component :as component]
            [appbone-service-template.http :as http]
            [appbone-service-template.db :as db])
  (:gen-class))

(defn create-service [config-opts]
  "wires up the web service's dependency graph with provided configuration"
  (let [{:keys [spec http-port]} config-opts]
    (component/system-map
     :config-opts config-opts
     :db (db/map->DB {})
     :http (component/using
            (http/map->HTTP {:spec spec
                             :port http-port})
            [:db]))))

(defn -main [& args]
  "entry point for executing outside of a REPL"
  (let [system (create-service {:spec "api-spec.yml"
                                :http-port 8080})]
    (component/start system)))

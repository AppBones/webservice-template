(ns appbone-service-template.core
  (:require [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [appbone-service-template.http :as http]
            [appbone-service-template.db :as db])
  (:gen-class))

(defn create-service [config-opts]
  "wires up the web service's dependency graph with provided configuration"
  (let [{:keys [spec http-port is-dev]} config-opts
        is-dev (if (nil? is-dev) (= "true" (env :is-dev)) is-dev)]
    (component/system-map
     :config-opts config-opts
     :db (db/map->DB {})
     :http (component/using
            (http/map->HTTP {:spec spec
                             :port http-port
                             :is-dev is-dev})
            [:db]))))

(defn -main [& args]
  "entry point for executing outside of a REPL"
  (let [port (if (nil? (env :port)) "8080" (env :port))
        system (create-service {:spec "api-spec.yml"
                                :http-port (read-string port)
                                :is-dev false})]
    (component/start system)))

(ns dev
  (:require [appbone-service-template.core :refer :all]
            [clojure.java.io :as io]
            [clojure.java.javadoc :refer [javadoc]]
            [clojure.pprint :refer [pprint]]
            [clojure.reflect :refer [reflect]]
            [clojure.repl :refer [apropos dir doc find-doc pst source]]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :as test]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]]
            [com.stuartsierra.component :as component]
            [midje.repl :refer :all]))

(def system nil)

(defn stop []
  (when-not (nil? system)
    (alter-var-root #'system component/stop))
  (def system nil))

(defn start []
  (when-not (nil? system)
    (stop))
  (let [config {:spec "api-spec.yml"
                :http-port 3000}]
    (def system (create-service config))
    (alter-var-root #'system component/start)))

(defn reset []
  (try
    (stop)
    (catch NullPointerException ex (def system nil)))
  (refresh :after 'dev/start))

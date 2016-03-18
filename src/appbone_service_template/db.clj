(ns appbone-service-template.db
  (:require [com.stuartsierra.component :as component]))

(defrecord DB [counter]
  component/Lifecycle

  (start [this]
    (println "Starting DB component ...")
    (assoc this :greetings (atom []) :counter (atom 0)))

  (stop [this]
    (assoc this :counter nil :greetings nil)))

(defn inc-counter [db]
  (-> db (get :counter) (swap! inc)))

(defn add-greeting [db greeting]
  (-> db (get :greetings) (swap! conj greeting)))

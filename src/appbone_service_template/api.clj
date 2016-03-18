(ns appbone-service-template.api
  (:require [clojure.data.json :as json]
            [ring.util.response :as r]
            [liberator.core :refer [resource]]
            [appbone-service-template.db :as db]))

(defn create-greeting
  "Generate a greeting message and return it."
  [request db]
  (let [name (get-in request [:parameters :query :name])
        message (str "Hello " name "!")]
    (assoc {:name name :message message} :counter (db/inc-counter db))))

(defn post-greeting
  [request db]
  (let [name (get-in request [:parameters :body :greeting :name])
        message (str "Hello" name "!")
        id (db/inc-counter db)]
    (db/add-greeting db { :name name :message message :counter id })))

(defn greeting [req db]
  (let [handler
        (resource
         :allowed-methods [:post :get]
         :available-media-types ["application/json"]
         :post! (post-greeting req db)
         :handle-ok (create-greeting req db))]
    (handler req)))

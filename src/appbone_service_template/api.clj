(ns appbone-service-template.api
  (:require [clojure.data.json :as json]
            [ring.util.response :as r]
            [liberator.core :refer [resource]]
            [appbone-service-template.db :as db]))

(defn parse-json [ctx key]
  "When ctx has verb PUT or POST, try to parse body as json and store it in ctx."
  (when (#{:put :post} (get-in ctx [:request :request-method]))
    (try
      (let [body (get-in ctx [:request :body])]
        (let [data (json/read-str body)]
          [false {key data}])
        {:message "No body"})
      (catch Exception e
        (.printStackTrace e)
        {:message (format "IOException: %s" (.getMessage e))}))))

(defn check-content-type [ctx content-types]
  "When ctx has verb PUT or POST, return true if the content-type is json."
  (if (#{:put :post} (get-in ctx [:request :request-method]))
    (or
     (some #{(get-in ctx [:request :headers "content-type"])}
           content-types)
     [false {:message "Unsupported Content-Type"}])
    true))

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
         :known-content-type? (check-content-type req ["application/json"])
         :malformed? (parse-json req :data)
         :post! (post-greeting req db)
         :handle-ok (create-greeting req db))]
    (handler req)))

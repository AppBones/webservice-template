(ns appbone-service-template.api
  (:require [clojure.data.json :as json]
            [ring.util.response :as r]
            [liberator.core :refer [resource]]
            [environ.core :refer [env]]
            [liberator.representation :refer [ring-response render-map-generic]]
            [appbone-service-template.db :as db]))

(defn handle-exception
  "Escape hatch for Liberator exceptions. Any logging, printing or recovery for
  exceptions goes here"
  [ctx]
  (let [e (:exception ctx)]
    (if (= "true" (env :is-dev))
      (clojure.repl/pst e)
      (print e "Liberator:" (.getClass e) "message:" (.getMessage e)))))

(defn media-types
  "Returns the media-types available in a context given the path, based on the
  Swagger definition"
  [spec]
  (->> spec
       (vals)
       (mapcat #(find % "produces"))
       (flatten)
       (set)
       (filterv #(not= "produces" %))))

(defn describe-resource
  "Injects the Swagger specification for the given path into the response body"
  [ctx path spec]
  (let [rep (get-in ctx [:request :headers "accept"])
        ctx (assoc ctx :representation {:media-type rep})
        body (render-map-generic {path spec} ctx)]
    (ring-response {:body body})))

(defn create-greeting
  "Generate a greeting message and return it"
  [ctx db]
  (let [name (get-in ctx [:request :parameters :query :name])
        message (str "Hello " name "!")]
    (assoc {:name name :message message} :counter (db/inc-counter db))))

(defn post-greeting
  "Post a greeting to the database"
  [ctx db]
  (let [name (get-in ctx [:request :parameters :body :greeting :name])
        message (str "Hello" name "!")
        id (db/inc-counter db)]
    (db/add-greeting db { :name name :message message :counter id })))

(defn greeting [ctx db path]
  (let [spec (get-in ctx [:swagger :context :definition "paths" path])
        handler
        (resource
         :allowed-methods (map keyword (keys spec))
         :available-media-types (media-types spec)
         :handle-created #(post-greeting % db)
         :handle-exception handle-exception
         :handle-options #(describe-resource % path spec)
         :handle-ok #(create-greeting % db))]
    (handler ctx)))

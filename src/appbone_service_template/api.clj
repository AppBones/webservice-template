(ns appbone-service-template.api
  (:require [ring.util.response :as r]
            [liberator.core :refer [resource]]
            [halresource.resource :as hal]
            [taoensso.timbre :as log]
            [liberator.representation :refer [ring-response render-map-generic]]
            [appbone-service-template.representation]
            [appbone-service-template.db :as db]))

(defn handle-exception
  "Escape hatch for Liberator exceptions. Any logging, printing or recovery for
  exceptions goes here."
  [ctx]
  (let [e (:exception ctx)]
    (log/error e)))

(defn self-href
  "Constructs a default link to the current resource based on the request map."
  [context]
  (let [p (get-in context [:request :server-port])
        port (if-not (or (= 80 p) (= 443 p)) p nil)
        protocol (name (get-in context [:request :scheme]))
        uri (get-in context [:request :uri])]
    (str protocol "://" (get-in context [:request :server-name]) ":" port uri)))

(defn media-types
  "Returns the media-types declared to be available in the Swagger definition
  of a resource."
  [spec]
  (->> spec
       (vals)
       (mapcat #(find % "produces"))
       (flatten)
       (set)
       (filterv #(not= "produces" %))))

(defn describe-resource
  "Injects the Swagger specification for the given path into the response body."
  [ctx path spec]
  (let [rep (get-in ctx [:request :headers "accept"])
        ctx (assoc ctx :representation {:media-type rep})
        body (render-map-generic {path spec} ctx)]
    (ring-response {:body body})))

(defn create-greeting
  "Generate a greeting message and return it."
  [ctx db]
  (let [name (get-in ctx [:request :parameters :query :name])
        message (str "Hello " name "!")]
    (assoc {:name name :message message} :counter (db/inc-counter db))))

(defn post-greeting!
  "Post a greeting to the database."
  [ctx db]
  (let [name (get-in ctx [:request :parameters :body :greeting :name])
        msg (str "Hello " name "!")
        id (db/inc-counter db)
        body (last (db/add-greeting db {:name name :message msg :counter id}))
        loc (str (self-href ctx) "/" id)]
    (-> ctx
        (assoc-in [:hal :href] loc)
        (assoc :data body))))

(defn greeting [ctx db path]
  (let [spec (get-in ctx [:swagger :context :definition "paths" path])
        handler
        (resource
         :initialize-context {:hal (hal/new-resource (self-href {:request ctx}))}
         :allowed-methods (map keyword (keys spec))
         :available-media-types (media-types spec)
         :post! #(post-greeting! % db)
         :post-redirect? false
         :handle-created #(let [l (get-in % [:hal :href])]
                            (ring-response (:data %) {:headers {"Location" l}}))
        :handle-exception handle-exception
        :handle-options #(describe-resource % path spec)
        :handle-ok #(create-greeting % db))]
  (handler ctx)))

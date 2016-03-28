(ns appbone-service-template.representation
  (:require [clojure.data.json :as json]
            [clojure.data.xml :as xml]
            [halresource.resource :as hal]
            [liberator.representation :refer [render-map-generic]]))

(defmethod render-map-generic "application/xml"
  [data context]
  (let [result (hal/add-properties {} data)]
    (-> result
        hal/xml-representation
        xml/sexp-as-element
        xml/emit-str)))

(defmethod render-map-generic "application/hal+json"
  [data context]
  (let [result (-> (:hal context)
                   (hal/add-properties data))]
    (hal/resource->representation result :json)))

(defmethod render-map-generic "application/hal+xml"
  [data context]
  (let [result (-> (:hal context)
                   (hal/add-properties data))]
  (hal/resource->representation result :xml)))


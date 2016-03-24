(ns appbone-service-template.util
  (:require [clojure.string :refer [replace-first]]
            [io.sarnowski.swagger1st.executor :as s1stexec]
            [clojure.string :as str]))


(defn operationId->func
  "Given a swagger request definition, resolves the operationId to the
  matching liberator resource function.

  operationIds are expected to be of the form:

     namespace/resource*extraneous-information

  where extraneous-information is ignored."
  [request-definition]
  (let [opId (get request-definition "operationId")]
    (s1stexec/function-by-name (first (str/split opId #"\*")))))

(defn extract-path
  "Given a request map containing the :swagger definition key, extract
  the relative path of the resource requested"
  [request]
  (let [base (get-in request [:swagger :context :definition "basePath"])
        path (:uri request)]
    (replace-first path base "")))

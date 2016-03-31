(ns appbone-service-template.util
  (:require [io.sarnowski.swagger1st.executor :as s1stexec]
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

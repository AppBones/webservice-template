(ns appbone-service-template.middleware
  (:require [liberator.dev :refer [wrap-trace]]))

(defn wrap-liberator-trace
  "Conditionally wrap handler with liberator trace enabled if running locally.

  This will append X-Liberator header to all responses for the purposes of debugging
  the decision tree. Additionally, debug information will be made available at
  the URI:

  /x-liberator/requests/
  "
  [handler]
  (fn [request]
    (if (= (:server-name request) "localhost")
      ((wrap-trace handler :header :ui) request)
      (handler request))))

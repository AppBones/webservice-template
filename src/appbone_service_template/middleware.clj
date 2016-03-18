(ns appbone-service-template.middleware
  (:require [environ.core :refer [env]]
            [liberator.dev :refer [wrap-trace]]))

(defn wrap-liberator-trace
  "Conditionally wrap handler with liberator trace enabled if running in dev profile.

  This will append X-Liberator header to all responses for the purposes of debugging
  the decision tree. Additionally, debug information will be made available at
  the URI:

  /x-liberator/requests/
  "
  [handler]
  (fn [request]
    (if (env :is-dev)
      ((wrap-trace handler :header :ui) request)
      (handler request))))

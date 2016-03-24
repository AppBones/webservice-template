(ns appbone-service-template.http
  (:require [io.sarnowski.swagger1st.core :as s1st]
            [com.stuartsierra.component :as component]
            [org.httpkit.server :refer [run-server]]
            [liberator.dev :refer [wrap-trace]]
            [environ.core :refer [env]]
            [appbone-service-template.util :refer :all]))

(defrecord HTTP [server db spec port]
  component/Lifecycle

  (start [this]
    (println "Starting HTTP component on port" port "...")
                                        ; the resolver-fn is the magic where you can pass in whatever you want to the API implementing functions,
                                        ; for example the DB dependency of this component. You could also restructure the parameters alltogether or
                                        ; define your own dynamic mapping scheme.
    (let [resolver-fn (fn [request-definition]
                        (if-let [cljfn (operationId->func request-definition)]
                          (fn [request]
                            (let [path (extract-path request)]
                                        ; Here we are actually calling our clojure functions with the parameters map in the first
                                        ; argument and our DB dependency as the second argument.
                            (cljfn request db path)))))

          handler (-> (s1st/context :yaml-cp spec)
                      (s1st/discoverer)
                      (s1st/mapper)
                      (s1st/parser)
                      (s1st/executor :resolver resolver-fn))

          handler (when (= "true" (env :is-dev))
                    (wrap-trace handler :header :ui))]

      (assoc this :server (run-server handler {:join? false
                                               :port port}))))

  (stop [this]
    ((:server this))
    (assoc this :server nil)))

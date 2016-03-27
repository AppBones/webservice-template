(ns appbone-service-template.http
  (:require [io.sarnowski.swagger1st.core :as s1st]
            [com.stuartsierra.component :as component]
            [org.httpkit.server :refer [run-server]]
            [liberator.dev :refer [wrap-trace]]
            [appbone-service-template.util :refer :all]))

(defrecord HTTP [server db spec port is-dev]
  component/Lifecycle

  (start [this]
    (println "Starting HTTP component on port" port "...")
    ;; The resolver-fn is the magic where you can pass in whatever you want to
    ;; the API implementing functions.
    ;; For example, the DB dependency of this component. You could also
    ;; restructure the parameters together or define your own mapping scheme.
    (let [resolver-fn (fn [request-definition]
                        (if-let [cljfn (operationId->func request-definition)]
                          (fn [request]
                            (let [path (extract-path request)]
                              ;; Here we are actually calling our handler
                              (cljfn request db path)))))

          handler (-> (s1st/context :yaml-cp spec)
                      (s1st/discoverer)
                      (s1st/mapper)
                      (s1st/parser)
                      (s1st/executor :resolver resolver-fn))

          handler (if is-dev
                    (wrap-trace handler :header :ui)
                    handler)]

      (assoc this :server (run-server handler {:join? false
                                               :port port}))))

  (stop [this]
    ((:server this))
    (assoc this :server nil)))

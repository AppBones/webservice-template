(ns appbone-service-template.api-test
  (:require appbone-service-template.api)
  (:use
   [io.sarnowski.swagger1st.context :only [create-context]]
   [ring.mock.request :as mock]
   [clojure.data.json :as json :only [read-str]]
   [clojure.walk :only [keywordize-keys]]
   [midje.sweet :refer :all]))

(def swagger (create-context :yaml-cp "api-spec.yml" true))
(def definition (get-in swagger [:definition "paths" "/greeting"]))
(def handler appbone-service-template.api/greeting)
(def db {:greetings (atom []) :counter (atom 0)})

(let [request (-> (mock/request :get "http://localhost:3000/api/greeting")
                  (assoc :parameters {:query {:name "World"}}))
      spec (get-in definition ["get"])
      response (handler request db spec)
      body (keywordize-keys (json/read-str (:body response)))]

  (facts "about GET"
         response => (contains {:status 200})
         response => (contains {:headers {"Content-Type" "application/hal+json;charset=UTF-8", "Vary" "Accept"}})
         body => {:name "World",
                  :message "Hello World!",
                  :counter 1,
                  :_links {
                           :self {
                                  :href "http://localhost:3000/api/greeting"
                                  }
                           }
                  }
         ))

(let [request (-> (mock/request :post "http://localhost:3000/api/greeting")
                  (assoc :parameters {:body {:greeting {:name "World"}}}))
      spec (get-in definition ["post"])
      response (handler request db spec)
      body (keywordize-keys (json/read-str (:body response)))]

  (facts "about POST"
         response => (contains {:status 201})
         response => (contains {:headers {
                                          "Content-Type" "application/hal+json;charset=UTF-8",
                                          "Location" "http://localhost:3000/api/greeting/2",
                                          "Vary" "Accept"}})
         body => {:name "World",
                  :message "Hello World!",
                  :counter 2,
                  :_links {
                           :self {
                                  :href "http://localhost:3000/api/greeting/2"
                                  }
                           }
                  }
         ))

(let [request (mock/request :options "http://localhost:3000/api/greeting")
      spec (get-in definition ["options"])
      response (handler request db spec)]

  (facts "about OPTIONS"
         response => (contains {:status 200})
         response => (contains {:headers {"Allow" "GET, POST, OPTIONS"}})
         ))

(ns appbone-service-template.api-test
  (:require appbone-service-template.api)
  (:use
   [liberator.representation :only [->when]]
   [ring.mock.request :as mock]
   [clojure.data.json :as json :only [read-str]]
   [clojure.walk :only [keywordize-keys]]
   [midje.sweet :only [fact facts against-background contains before]]))

(facts "about GET"
       (let [handler appbone-service-template.api/greeting
             request (-> (mock/request :get "http://localhost:3000/api/greeting")
                         (assoc :parameters {:query {:name "World"}}))
             db {:greetings (atom []) :counter (atom 0)}
             spec {"produces" ["application/hal+json"]}
             response (handler request db spec)
             body (keywordize-keys (json/read-str (:body response)))]
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

(facts "about POST"
       (let [handler appbone-service-template.api/greeting
             request (-> (mock/request :post "http://localhost:3000/api/greeting")
                         (assoc :parameters {:body {:greeting {:name "World"}}}))
             db {:greetings (atom []) :counter (atom 0)}
             spec {"produces" ["application/hal+json"]}
             response (handler request db spec)
             body (keywordize-keys (json/read-str (:body response)))]
         response => (contains {:status 201})
         response => (contains {:headers {
                                          "Content-Type" "application/hal+json;charset=UTF-8",
                                          "Location" "http://localhost:3000/api/greeting/1",
                                          "Vary" "Accept"}})
         body => {:name "World",
                  :message "Hello World!",
                  :counter 1,
                  :_links {
                           :self {
                                  :href "http://localhost:3000/api/greeting/1"
                                  }
                           }
                  }
         ))

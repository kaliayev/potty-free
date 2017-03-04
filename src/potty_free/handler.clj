(ns potty-free.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [gpio.core :as gpio]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]])
  (:gen-class))

#_(def gpio-port
  (gpio/open-channel-port 17))

(def server
  "http://localhost:3000/potty")

(def timeout
  10000)

#_(defn parse-gpio
  [port]
  (let [value (gpio/read-value port)]
    #_(cond (= value :low) 0
            (= value :high) 127)
    value))

#_(defn state
  [port]
  (let [gpio-val (parse-gpio port)]
    (con)))

(defn -main
  []
  (try 
    (try (loop []
           (do (println (str "It's been " timeout " seconds"))
               (http/post server {:body (json/generate-string {:timeout timeout})
                                  :content-type :json
                                  :as :json})
               #_(println (http/post server {:body "{\"json\": \"input\"}"
                                           :headers {"Content-Type" "application/json"}}))
               (Thread/sleep timeout)
               (recur)))
         (catch Exception e (println e)))))

(ns potty-free.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [gpio.core :as gpio]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]])
  (:gen-class))

(def default-tid 1)

(def gpio-port
  (gpio/open-channel-port 17))

(def server
  "http://dcb0e416.ngrok.io")

(def timeout
  5000)

(defn parse-gpio
  [port]
  (let [value (gpio/read-value port)]
    (cond (= value :high) :occupied
          (= value :low) :free)))

#_(defn state
  [port]
  (let [gpio-val (parse-gpio port)]
    (con)))

(defn -main
  [& args]
  (let [tid (or (first args) default-tid)]
    (try (loop []
           (do (http/post server {:body (json/generate-string {:state (parse-gpo gpio-port)
                                                               :toilet tid})
                                  :throw-exceptions false
                                  :content-type :json
                                  :as :json}) 
               (Thread/sleep timeout)
               (recur)))
         (catch Exception e (println e)))))

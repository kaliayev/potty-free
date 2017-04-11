(ns potty-free.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [gpio.core :as gpio]
            [clojure.core.async :as a]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]])
  (:gen-class))

(def this-tid "third")

(def port-num 17)

(def server
  (str "http://fb71d3f7.ngrok.io/toilet/" this-tid))

(def timeout
  5000)

(defn parse-value
  [value]
  (cond (= value :high) false
        (= value :low) true))

(defn -main
  [& args]
  (let [tid (or (first args) this-tid)]
    (a/go (loop []
            (let [gpio-port (gpio/open-channel-port port-num)
                  _ (gpio/set-direction! gpio-port :in)
                  _ (gpio/set-edge! gpio-port :both)
                  ch (gpio/create-edge-channel gpio-port)]
              (when-let [value (a/<! ch)]
                (http/post server {:body (json/generate-string
                                          {:state (parse-value value)})
                                   :throw-exceptions false
                                   :content-type :json
                                   :as :json}))
              (a/close! ch)
              (gpio/close! gpio-port)
              (println "port " port-num " is closed")
              (Thread/sleep timeout))
            (recur)))))

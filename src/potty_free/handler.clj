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

(defmacro add-shutdown-hook
  "Add shutdown hooks to runtime. Can be called multiple times."
  [& body]
  `(.. Runtime getRuntime (addShutdownHook
                           (Thread. (fn [] ~@body)))))

(defn parse-value
  [value]
  (cond (= value :high) false
        (= value :low) true))

(defn process-gpio-chan
  [ch gpio-port]
  (loop []
    (when-let [value (a/<!! ch)]
      (println (str "\n\n"
                    (-> gpio-port
                        gpio/read-value)
                    "\n"))
      (http/post server {:body (json/generate-string
                                {:state (-> gpio-port
                                            gpio/read-value
                                            parse-value)})
                         :throw-exceptions false
                         :content-type :json
                         :as :json}))
    (recur)))

(defn -main [& args]
  (let [tid (or (first args) this-tid)
        gpio-port (-> (gpio/open-channel-port port-num)
                      (gpio/set-direction! :in)
                      (gpio/set-edge! :both))
        gpio< (gpio/create-edge-channel gpio-port)]
    (add-shutdown-hook (do (a/close! gpio<)
                           (gpio/close! gpio-port)))
    (try (process-gpio-chan gpio< gpio-port)
         (catch Throwable e
           (println (pr-str e) "\n\nUser Terminated!!!"))
         (finally (do (a/close! gpio<)
                      (gpio/close! gpio-port))))))

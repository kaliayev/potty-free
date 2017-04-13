(ns potty-free.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [gpio.core :as gpio]
            [clojure.core.async :as a]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]])
  (:gen-class))

(def this-tid "1")

(def port-num 17)

(def host
  "https://us-east-1-dev-pnq.cxengagelabs.net/toilet/")

(def server
  (str "https://us-east-1-dev-pnq.cxengagelabs.net/toilet/" this-tid))

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
  [ch gpio-port url]
  (loop []
    (when-let [value (a/<!! ch)]
      (try (let [body (json/generate-string
                       {:status (-> gpio-port
                                    gpio/read-value
                                    parse-value)})
                 _ (println (str "\n\n" body "\n"))
                 result (http/post url {:body body
                                        :throw-exceptions false
                                        :content-type :json
                                        :as :json})]
             (println (str "\n\n" result "\n")))
           (catch Throwable t
             (println (pr-str t) "\n\nPost Request Failed."))))
    (recur)))

(defn -main [& args]
  (let [tid (or (first args) this-tid)
        host (or (second args) host)
        url (str host tid)
        _ (println "Host: " host "\nUrl: " url)
        gpio-port (-> (gpio/open-channel-port port-num)
                      (gpio/set-direction! :in)
                      (gpio/set-edge! :both))
        gpio< (gpio/create-edge-channel gpio-port)]
    (add-shutdown-hook (do (a/close! gpio<)
                           (gpio/close! gpio-port)
                           (println "\n\nUser Terminated!!!")))
    (try (process-gpio-chan gpio< gpio-port url)
         (catch Throwable e
           (println (pr-str e) "\n\nUser Terminated!!!"))
         (finally (do (a/close! gpio<)
                      (gpio/close! gpio-port))))))

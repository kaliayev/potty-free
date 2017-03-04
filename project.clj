(defproject potty-free "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [clj-gpio "0.2.0"]
                 [cheshire "5.7.0"]
                 [clj-http "2.3.0"]]
  :plugins [[lein-ring "0.9.7"]]
  :main potty-free.handler
  :ring {:handler potty-free.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}
   :uberjar {:aot :all}})

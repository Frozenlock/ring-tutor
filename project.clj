(defproject org.clojars.frozenlock/ring-tutor "0.1.2"
  :description "Ring middleware to add tutorials to your ring websites."
  ;:url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clout "1.2.0"]]
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler ring-tutor.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]
                        [compojure "1.1.8"]
                        [kerodon "0.4.0"]]
         :source-paths ["src" "src-dev"]}})

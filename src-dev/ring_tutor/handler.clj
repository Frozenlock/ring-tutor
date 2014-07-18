(ns ring-tutor.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring-tutor.core :as tutor]
            [ring.util.response :as resp]))

(def demo-tutor
  [{:uri "/2"
    :step-fn #(str "<br><div id='tutor-step-2'>Tutor for " (:uri %) " !!</div><br>")}
   {:uri "/"
    :step-fn #(str "<br><div id='tutor-step-1'>Tutor for " (:uri %) " !!</div><br>")}])

(defroutes app-routes
  (GET "/" req (str "<h1>Hello World</h1><br><a href=/1>next page</a>" (tutor/generated-tutor req)))
  (GET "/1" req (str "<h1>Page 1</h1><br><a href=/2>next page</a>" (tutor/generated-tutor req)))
  (GET "/2" req (str "<h1>Page 2</h1><br><a href=/3>next page</a>" (tutor/generated-tutor req)))
  (GET "/3" req (str "<h1>Page 3</h1><br><a href=/4>next page</a>"(tutor/generated-tutor req)))
  (GET "/4" req (str "<h1>Page 4</h1><br><p>some other page</p>"(tutor/generated-tutor req)))
  (GET "/tutor" []
    (-> (resp/redirect "/")
        (tutor/set-tutor-sequence demo-tutor)
        ;(tutor/set-default-wrong-uri-fn (constantly "WRONG URI!!!"))
        ))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (tutor/wrap-tutor)
      (handler/site)))

(ns ring-tutor.test.tutorial
  (:use [kerodon.core]
        [kerodon.test]
        [clojure.test])
  (:require [ring-tutor.handler :as handler]))


(deftest user-tutorial-steps
  (-> (session handler/app)
      (visit "/")
      (has (missing? [:#tutor-step-1])
           "Tutorial not yet activated")
      (visit "/tutor") ;; now we activate the tutorial
      (follow-redirect) ;; redirect back to home page
      (has (attr? [:div] :id "tutor-step-1"))
      (follow "next page")
      (has (missing? [:#tutor-step-2])) ;; shouldn't be step 2 at this url
      (has (regex? #".*Oops.*")) ;; default tutorial 'error' message
      (follow "next page")
      (has (attr? [:div] :id "tutor-step-2")) ;; now we should be at the step 2
      (visit "/")
      (has (missing? [:#tutor-step-1])
           "Tutorial should be completed")))
      
      

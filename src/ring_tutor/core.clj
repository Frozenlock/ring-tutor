(ns ring-tutor.core
  (:require [clout.core :as clout]))

;; example tutor step
(comment
  {:uri "/1"
   :step-fn (constantly "<h2>test tutor</h2>")
   :wrong-uri-fn (constantly "<h2>Wrong URI!</h2>" ;;optional
                             )})

(defn tutor-active?
  "Is there any tutor steps available?" [request]
  (not-empty (get-in request [:session ::ring-tutor :steps])))

(defn- current-step
  "Return the current tutor step."
  [request]
  (-> (get-in request [:session ::ring-tutor :steps])
      (peek)))


(defn set-default-wrong-uri-fn
  "Set the default generating function to apply to the request when
  the user is at the wrong URI in an active tutor session."
  [response wrong-uri-fn]
  (assoc-in response [:session ::ring-tutor :wrong-uri-fn] wrong-uri-fn))

(defn- get-default-wrong-uri-fn
  "Get the default generating function to apply to the request when
  the user is at the wrong URI in an active tutor session."
  [request]
  (get-in request [:session ::ring-tutor :wrong-uri-fn]))

(defn- wrong-uri-fn
  "Return a wrong-uri-fn. Priority, in order:
   1. :wrong-uri-fn defined in the current step;
   2. wrong-uri-fn defined using the `set-default-wrong-uri-fn' function;
   3. hardcoded wrong-uri-fn."
  [request]
  (or (:wrong-uri-fn (current-step request))
      (get-default-wrong-uri-fn request)
      (constantly
       (str "Oops, bad address... you were supposed to be at <a href="
            (:uri (current-step request))"> this location</a>."))))

(defn- next-step
  "Return the next tutor step from the :ring-tutor session."
  [request]
  (-> (get-in request [:session ::ring-tutor :steps])
      (pop)
      (peek)))


(defn- match-current-step?
  "Return non-nil if the current URI is the one expected for the
  current tutor step." [request]
  (clout/route-matches (:uri (current-step request)) request))


(defn- update-tutor
  "Update the tutor session (if needed) and return the updated request."
  [request]
  (cond
   (match-current-step? request) (assoc-in request [:session ::ring-tutor :generated]
                                           ((:step-fn (current-step request)) request))
   :else (assoc-in request [:session ::ring-tutor :generated]
                   ((wrong-uri-fn request) request))))

(defn- drop-current-step
  "Return the response with the next step removed from the :ring-tutor
  session." [tutor-state]
  (->> (get tutor-state :steps)
       (pop)
       (assoc tutor-state :steps)))


(defn generated-tutor
  "Get the generated tutor step from the request (if any)."
  [request]
  (get-in request [:session ::ring-tutor :generated]))

(defn set-tutor-sequence
  "Return a new response with the tutor sequence added to the user's
  session." [response tutor-sequence]
  (assoc-in response [:session ::ring-tutor :steps] tutor-sequence))




;;; Because of the way 'session' works, we need to read our data and
;;; update the consumable in the -request-. Any update to the tutor
;;; state, however, must be given in the -response- for it to be
;;; updated in the session.

(defn wrap-tutor [handler]
  (fn [request]
    (if (tutor-active? request)            
      (let [current-tutor-state (get-in request [:session ::ring-tutor])
            match? (match-current-step? request) ; does the URI match? (must be checked in the request)
            ;; generate the consumable for the handler
            response (-> request
                         (update-tutor)
                         (handler))]
        ;; now we update the tutor state as needed
        (if match?
          (assoc-in response [:session ::ring-tutor]
                    (drop-current-step current-tutor-state))
          response))
      (handler request)))) ; if we don't have a tutor-state, just evaluate as usual

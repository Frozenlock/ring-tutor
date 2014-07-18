# Ring-Tutor

Ring middleware to add tutorials to your ring websites.

Automatically discards completed steps and makes sure the user is at
the correct URI.

<img src="https://raw.githubusercontent.com/Frozenlock/ring-tutor/master/350px-Johannes_Kepler_1610.jpg"
 alt="Johannes Kepler" title="Johannes Kepler"/>


## Usage

Add `[org.clojars.frozenlock/ring-tutor "0.1.1"]` to your `project.clj` dependencies.

This library also requires the *ring.middleware.session* middleware to be used in your ring-app.

### Adding the Middleware

Make sure to add the `ring-tutor` middleware *inside* the ring-session middleware:

```clj
(def app
  (-> app-routes
      (tutor/wrap-tutor); <--- tutor middleware
      (handler/site)))  ; <--- session middleware
```

### Making Tutorial Steps
A tutorial is nothing else than a sequence of maps:

```clj
(def demo-tutor
  [{:uri "/2"
    :step-fn #(str "<br><div id='tutor-step-2'>Tutor for " (:uri %) " !!</div><br>")}
   {:uri "/"
    :step-fn #(str "<br><div id='tutor-step-1'>Tutor for " (:uri %) " !!</div><br>")}])
```
(because we `pop` each step, the tutorial is in reverse order)

The 3 possible keywords for each step maps:

- `:uri`: is the expected URI for the given step;
- `:step-fn`: the function to generate the current tutorial step (with the ring request as the argument);
- `:wrong-uri-fn`: the function to generate an 'error' message if the
  user finds himself at the wrong URI (again, with the ring request as
  the argument). If nothing is provided, default to the user provided
  error function (set with `set-default-wrong-uri-fn`) or to the
  library default.;

### Adding and Removing Steps in the User Session

Any updates to the `tutor` state must be done with the ring *response*.

```clj
(POST "/tutor" []
    (-> (resp/redirect "/") ; create the ring response
        (tutor/set-tutor-sequence demo-tutor)))
```

```clj
(POST "/clear-tutor" []
    (-> (resp/redirect "/")
        (tutor/set-tutor-sequence nil))) ;; this clears the tutorial
```


To get access to the generated content in your webpages, use the
function `generated-tutor` with the request as the argument.

```clj
(GET "/" req (str "<h1>Hello World</h1><br><a href=/1>next page</a>" (tutor/generated-tutor req)))
```

A nice place to use this function is in the common rendering function
we usually find in the ring/compojure projects.

## License

Copyright © 2014 Frozenlock

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

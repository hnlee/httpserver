(ns cob-spec-app.core
  (:require [cob-spec-app.routes :as routes]
            [httpserver.core :as httpserver]))

(defn run-server [port dir]
  (httpserver/run {:port port
                   :dir dir
                   :router routes/choose-response}))
    
(defn -main
  [& args]
  )

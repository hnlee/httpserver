(ns cob-spec-app.core
  (:require [cob-spec-app.routes :as routes]
            [httpserver.socket :as socket]
            [httpserver.core :as httpserver])
  (:gen-class))

(defn run-server [server dir]
  (httpserver/threading server
                        dir
                        routes/choose-response))
    
(defn -main [& args]
  (let [{port :port
         dir :dir} (httpserver/set-vars args)
        server (socket/open port)]
    (while (.isBound server)
      (run-server server dir))
    (socket/close server)))

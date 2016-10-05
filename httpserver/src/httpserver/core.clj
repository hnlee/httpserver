(ns httpserver.core
  (:gen-class)
  (:require [httpserver.router :as router]
            [httpserver.logging :as logging]
            [httpserver.socket :as socket]))

(def default-port 5000)

(def default-dir 
  (let [public (System/getenv "PUBLIC_DIR")]
    (if (nil? public) "." public)))

(defn set-vars [args]
  (let [flags (apply hash-map args)]
    {:port (Integer. (get-in flags ["-p"] default-port))
     :dir (get-in flags ["-d"] default-dir)}))

(defn serve [connection dir]
  (try 
    (let [client-msg (socket/receive connection)]
      (logging/log-request client-msg dir)
      (socket/give connection
                   (router/choose-response client-msg
                                           dir)))
    (finally (socket/close connection))))

(defn threading [server dir]
  (let [connection (socket/listen server)
        thread (future (serve connection dir))]
    (while (not (.isClosed connection))
      (threading server dir)))) 

(defn -main [& args]
  (let [vars (set-vars args)
        server (socket/open (vars :port))]
    (while (.isBound server) 
      (threading server (vars :dir)))
    (socket/close server))) 

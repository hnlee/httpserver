(ns httpserver.core
  (:gen-class)
  (:require [httpserver.router :as router]
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
  (let [client-msg (socket/receive connection)
        server-msg (router/choose-response client-msg 
                                           dir)]
    (socket/give connection server-msg)))

(defn -main [& args]
  (let [vars (set-vars args)
        server (socket/open (vars :port))]
    (while true 
      (let [connection (socket/listen server)]
        (try (serve connection (vars :dir))
             (finally (socket/close connection)))))
    (socket/close server))) 


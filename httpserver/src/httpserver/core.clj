(ns httpserver.core
  (:gen-class)
  (:require [httpserver.router :as router]
            [httpserver.routes :as routes]
            [httpserver.logging :as logging]
            [httpserver.socket :as socket]))

(def default-port 5000)

(def default-dir 
  (let [public (System/getenv "PUBLIC_DIR")]
    (if (nil? public) "." public)))

(defn set-vars [args]
  (let [flags (apply hash-map args)]
    {:port (Integer. (get-in flags ["-p"] default-port))
     :dir (get-in flags ["-d"] default-dir)
     :router (get-in flags ["-r"] nil)}))

(defn route 
  ; Optional parameter to supply custom routes 
  ([client-msg dir]
    (router/choose-response client-msg dir))
  ([client-msg dir router-fn]
    (let [custom-route (router-fn client-msg dir)]
      (if-not (nil? custom-route) custom-route 
        (router/choose-response client-msg 
                                dir)))))

(defn serve [connection dir router-fn]
  (try 
    (let [client-msg (socket/receive connection)
          server-msg (if (nil? router-fn) (route client-msg
                                                 dir)
                       (route client-msg dir router-fn))]
      (logging/log-request client-msg dir)
      (socket/give connection server-msg))
    (finally (socket/close connection))))

(defn threading [server dir router-fn]
  (let [connection (socket/listen server)
        thread (future (serve connection dir router-fn))]
    (while (not (.isClosed connection))
      (threading server dir)))) 

(defn run [vars]
  (let [{port :port
         dir :dir
         router-fn :router} vars
        server (socket/open port)]
    (while (.isBound server)
      (threading server dir router-fn))
    (socket/close server)))

(defn -main [& args]
  (let [vars (set-vars args)]
    (run vars)))

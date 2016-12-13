(ns httpserver.core
  (:gen-class)
  (:require [httpserver.router :as router]
            [httpserver.logging :as logging]
            [httpserver.socket :as socket]))

;; require could be rewritten as
;; (:require (httpserver [core :as router]
;;                       [logging :as logging]
;;                       [socket :as socket]))

(def default-port 5000)

(defn getenv [env-var]
  (System/getenv env-var))

(defn default-dir []
  (let [public (getenv "PUBLIC_DIR")]
    (if (nil? public) "." public)))

(defn get-vars [args]
  (let [flags (apply hash-map args)]
    {:port (Integer. (get-in flags ["-p"] default-port)) ;; what happenes when Integer. is called on a string?
     :dir (get-in flags ["-d"] (default-dir))
     :router (get-in flags ["-r"] nil)}))

(defn route
  ; Optional parameter to supply custom routes
  ([client-msg dir]
    (router/choose-response client-msg dir))
  ([client-msg dir router-fn]
    (let [custom-route (router-fn client-msg dir)] ;; whitespace saved on next line
      (if-not (nil? custom-route) 
        custom-route
        (router/choose-response client-msg
                                dir)))))

(defn serve [connection dir router-fn]
  (try
    (let [client-msg (socket/receive connection) ;; whitespace saved on next line
          server-msg (if (nil? router-fn) ;; this should be pulled out into a separate function rather than having a conditional in a let binding
                       (route client-msg dir)
                       (route client-msg dir router-fn))]
      (logging/log-request client-msg dir)
      (socket/give connection server-msg))
    (finally (socket/close connection))))

(defn threading [server dir router-fn]
  (let [connection (socket/listen server)
        thread (future (serve connection dir router-fn))]
    (while (not (.isClosed connection))
      (threading server dir router-fn))))

(defn -main [& args]
  (let [{port :port
         dir :dir
         router-fn :router} (get-vars args)
        server (socket/open port)]
    (while (.isBound server)
      (threading server dir router-fn))
    (socket/close server)))

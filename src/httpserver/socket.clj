(ns httpserver.socket)

(comment (ns httpserver.socket
  (:import (java.net ServerSocket Socket SocketException))
  (:require [clojure.java.io :as io]))

(defn open [port]
  (ServerSocket. port))

(defn listen [server]
  (.accept server))
        
(defn reader [client]
  (io/reader (.getInputStream client)))
  
(defn writer [client]
  (io/writer (.getOutputStream client)))

(defn receive [reader]
  (loop [request-line ""
         line (.readLine reader)]
    (if (= 0 (count line)) request-line 
      (recur (str request-line line) 
             (.readLine reader)))))
    

(defn give [status-line writer]
  (.write writer status-line)
  (.flush writer))

(defn close [open-socket]
  (.close open-socket)))

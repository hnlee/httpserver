(ns httpserver.logging
  (:import (java.time LocalDateTime))
  (:require [httpserver.router :as router]))

(defn current-time [] (LocalDateTime/now))

(defn log-request [client-msg dir]
  (let [log-path (str dir "/logs")
        log (str (current-time) 
                 "\r\n" 
                 client-msg
                 "\r\n-----\r\n\r\n")]
    (spit log-path log :append true)))

(defn clear-log [dir]
  (let [log-path (str dir "/logs")]
    (spit log-path "" :append false)))

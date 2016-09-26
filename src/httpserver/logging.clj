(ns httpserver.logging
  (:import (java.time LocalDateTime))
  (:require [httpserver.router :as router]))

(defn log-request [client-msg dir]
  (let [log-path (str dir "/logs")
        log (str (LocalDateTime/now) 
                 ": " 
                 client-msg
                 "---")]
    (spit log-path log :append true)))

(defn clear-log [dir]
  (let [log-path (str dir "/logs")]
    (spit log-path "" :append false)))

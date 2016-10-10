(ns httpserver.logging
  (:import (java.time LocalDateTime))
  (:require [httpserver.router :as router]))

(defn log-request [client-msg dir]
  (let [log-path (str dir "/logs")
        log (str (LocalDateTime/now)  ; Gets a lot easier to test if you inject time, which is the devil
                 "\r\n"
                 client-msg
                 "-----\r\n")]
    (spit log-path log :append true)))

(defn clear-log [dir]
  (let [log-path (str dir "/logs")]
    (spit log-path "" :append false)))

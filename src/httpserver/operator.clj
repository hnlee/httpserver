(ns httpserver.operator
  (:require [httpserver.socket :as socket]
            [httpserver.request :as request]
            [httpserver.response :as response]))

(defn choose-response [client-request]
  (response/compose 200))

(defn serve [connection]
  (let [client-request (socket/receive connection)
        server-response (choose-response client-request)]
    (socket/give connection server-response)))


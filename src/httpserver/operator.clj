(ns httpserver.operator
  (:require [httpserver.socket :as socket]
            [httpserver.request :as request]
            [httpserver.response :as response]))

(defn get? [client-request]
  (= "GET" ((request/parse client-request) :method)))

(defn choose-response [client-request]
  (if (get? client-request)
    (response/compose (response/compose 200))))

(defn serve [connection]
  (let [client-request (socket/receive connection)
        server-response (choose-response client-request)]
    (socket/give connection server-response)))


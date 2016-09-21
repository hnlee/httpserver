(ns httpserver.operator
  (:require [clojure.java.io :as io]
            [httpserver.socket :as socket]
            [httpserver.request :as request]
            [httpserver.response :as response]
            [httpserver.router :as router]))

(defn not-found? [path]
  (not (.exists (io/as-file path)))) 

(defn choose-response [client-request dir]
  (let [msg (request/parse client-request)
        method (msg :method)
        uri (msg :uri)
        path (str dir (msg :uri))]
    (cond
      (and (contains? router/routes 
                      method)
           (contains? (router/routes method) 
                      uri)) (apply 
                              response/compose
                              ((router/routes method) uri)) 
      (not-found? path) (response/compose 404)
      (= method "HEAD") (response/compose 200)
      (= method "GET") (response/compose 
                         200
                         {"Content-Type" 
                          (response/content-type path)}
                         (response/content path)))
))
 
(defn serve [connection dir]
  (let [client-request (socket/receive connection)
        server-response (choose-response client-request
                                         dir)]
    (socket/give connection server-response)))


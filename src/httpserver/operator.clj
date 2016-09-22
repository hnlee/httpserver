(ns httpserver.operator
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [httpserver.socket :as socket]
            [httpserver.request :as request]
            [httpserver.response :as response]
            [httpserver.router :as router]))

(defn not-found? [path]
  (not (.exists (io/as-file path)))) 

(defn decode-uri [uri]
  (string/replace uri 
                  #"(?i)%[0-9a-f]{2}"
                  #(str (char (Integer/parseInt (subs % 1) 
                                                16)))))

(defn parse-parameters [parameters]
  (if 
    (string/includes?
      parameters 
      "=") (apply merge
                  (map #(apply hash-map 
                               (map decode-uri 
                                    (string/split % #"=")))
                       (string/split parameters #"&")))
    parameters))

(defn parse-query [uri]
  (if-let [uri-query (re-find #"(.*)\?(.*)$" uri)]
    {:uri (decode-uri (uri-query 1))
     :query (parse-parameters (uri-query 2))} 
    {:uri (decode-uri uri)
     :query ""}))

(defn choose-response [client-request dir]
  (let [msg (request/parse client-request)
        method (msg :method)
        uri (msg :uri)
        uri-query (parse-query uri)
        route (router/check-routes method 
                                   (uri-query :uri) 
                                   (uri-query :query))
        path (str dir (uri-query :uri))]
    (cond
      ((complement nil?) route) (apply response/compose 
                                       route)
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


(ns httpserver.operator
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [httpserver.socket :as socket]
            [httpserver.request :as request]
            [httpserver.response :as response]
            [httpserver.router :as router]))

(defn not-found? [path]
  (not (.exists (io/as-file path)))) 

(defn parse-query [uri]
  (if-let [query (re-find #"\?(.*)$" uri)]
    (string/replace (string/replace (query 1) #"=" " = ")
                    #"&"
                    "\r\n")
    nil))

(defn decode-uri [uri]
  (string/replace uri 
                  #"(?i)%[0-9a-f]{2}"
                  #(str (char (Integer/parseInt (subs % 1) 
                                                16)))))

(defn choose-response [client-request dir]
  (let [msg (request/parse client-request)
        method (msg :method)
        uri (decode-uri (msg :uri))
        path (str dir (msg :uri))]
    (cond
      (and (contains? router/routes 
                      method)
           (contains? (router/routes method) 
                      uri)) (apply 
                              response/compose
                              ((router/routes method) uri)) 
      ((complement nil?) 
        (parse-query uri)) (response/compose
                             200
                             {"Content-Type" "text/plain"}
                             (parse-query uri))
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


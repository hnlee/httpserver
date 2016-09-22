(ns httpserver.router
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [httpserver.socket :as socket]
            [httpserver.request :as request]
            [httpserver.response :as response]
            [httpserver.routes :as routes]))

(defn not-found? [path]
  (not (.exists (io/as-file path)))) 

(defn decode-uri [uri]
  (string/replace uri 
                  #"(?i)%[0-9a-f]{2}"
                  #(str (char (Integer/parseInt (subs % 1) 
                                                16)))))

(defn parse-parameters [parameters]
  (if (string/includes? 
        parameters 
        "=") (as-> parameters vars 
                  (string/split vars #"&")
                  (map #(string/split % #"=") vars)
                  (reduce concat vars)
                  (map decode-uri vars)
                  (apply hash-map vars))
    (decode-uri parameters)))
 
(defn parse-query [uri]
  (if-let [[uri base-uri query] 
           (re-find #"(.*)\?(.*)$" uri)]
    {:uri (decode-uri base-uri) 
     :query (parse-parameters query)} 
    {:uri (decode-uri uri)
     :query ""}))

(defn choose-response [client-request dir]
  (let [{method :method
         uri :uri} (request/parse client-request)
        {decoded-uri :uri
         parsed-query :query} (parse-query uri)
        route (routes/check-routes method 
                                   decoded-uri
                                   parsed-query)
        path (str dir decoded-uri)]
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


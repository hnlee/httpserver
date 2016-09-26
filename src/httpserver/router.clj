(ns httpserver.router
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [httpserver.request :as request]
            [httpserver.response :as response]
            [httpserver.routes :as routes]))

(def http-methods
  #{"OPTIONS" 
    "GET" 
    "HEAD"  
    "POST"  
    "PUT"  
    "DELETE"  
    "TRACE" 
    "CONNECT"})

(defn not-allowed? [method]
  (not (contains? http-methods method)))

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

(defn standard-get [path]
  (response/compose 200
                    {}
                    (response/content path)))

(defn credentials? [headers credentials]
  (and (contains? headers "Authorization")
       (= (headers "Authorization")
          (str "Basic " credentials))))

(defn authorize [headers credentials path]
  (if (credentials? headers
                    credentials) (standard-get path)
   (response/compose 401
                     {"WWW-Authenticate"
                      "Basic realm=\"Admin\""}))) 

(defn choose-response [client-msg dir]
  (let [{method :method
         uri :uri
         headers :headers} (request/parse client-msg)
        {decoded-uri :uri
         parsed-query :query} (parse-query uri)
        route (routes/check-routes method 
                                   decoded-uri
                                   parsed-query
                                   headers)
        credentials (routes/restricted method
                                       decoded-uri)
        path (str dir decoded-uri)]
    (cond
      (not-allowed? method) (response/compose 405)
      ((complement nil?) route) (apply response/compose 
                                       route)
      ((complement nil?) credentials) (authorize headers
                                                 credentials
                                                 path)
      (not-found? path) (response/compose 404)
      (= method "HEAD") (response/compose 200)
      (= method "GET") (standard-get path))))


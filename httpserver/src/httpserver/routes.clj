(ns httpserver.routes
  (:require [httpserver.encoding :as code] 
            [httpserver.request :as request]
            [httpserver.response :as response]
            [httpserver.file :as file]
            [httpserver.router :as router]
            [clojure.string :as string]
            [clojure.java.io :as io]))

(def static-routes
  {"GET" {"/redirect" 
          [302 {"Location" "http://localhost:5000/"}]
          "/coffee" 
          [418 {} "I'm a teapot"]
          "/tea" [200] 
         }
   "PUT" {"/file1" [405]} 
   "POST" {"/text-file.txt" [405]}
   "OPTIONS" {"/method_options2" 
              [200 {"Allow" "GET,OPTIONS"}]
              "/method_options" 
              [200 {"Allow" 
                    "GET, HEAD, POST, OPTIONS, PUT"}]}
}) 

(defn handle-form [method path body]
  (cond 
    (contains? #{"POST" "PUT"} 
               method) (do
                         (spit path 
                               body
                               :append false)  
                         (response/compose 200
                                           {}
                                           body))
    (= "DELETE" method) (do
                          (io/delete-file path)
                          (response/compose 200))
    (and (file/not-found? path)
         (= "GET" method)) (response/compose 200)
    (= "GET" method) (response/compose 
                       200
                       {}
                       (response/content path))))

(defn parameters? [method uri]
  (and (= method "GET")
       (= uri "/parameters")))

(defn restricted [method uri]
  (if (and (= method "GET")
           (= uri "/logs")) (code/encode-base64 
                              "admin:hunter2")))

(defn format-query [query]
  (if (string? query) query 
    (string/join "\r\n" (map #(str % " = " (query %))
                             (keys query)))))

(defn route? [method uri]
  (and (contains? static-routes method)
       (contains? (static-routes method) uri)))

(defn check-routes [client-msg dir]
  (let [{method :method
         uri :uri
         headers :headers
         body :body} (request/parse client-msg)
        {decoded-uri :uri
         query :query} (router/parse-query uri)
        path (str dir decoded-uri)
        credentials (restricted method
                                decoded-uri)]
  (cond 
    (route? method 
            decoded-uri) (apply response/compose 
                                ((static-routes method) decoded-uri))
    (parameters? method 
                 decoded-uri) (response/compose 
                                200
                                {} 
                                (format-query query))
    (= "/form" decoded-uri) (handle-form method
                                         path
                                         body)
    ((complement nil?) credentials) (router/authorize
                                       headers
                                       credentials
                                       path)
    :else nil)))

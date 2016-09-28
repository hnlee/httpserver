(ns httpserver.routes
  (:require [httpserver.encoding :as code] 
            [httpserver.response :as response]
            [httpserver.file :as file]
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
              [200 {"Allow" "GET,HEAD,POST,OPTIONS,PUT"}]}
}) 

(defn handle-form [method path body]
  (cond 
    (contains? #{"POST" "PUT"} 
               method) (do
                         (spit path 
                               body
                               :append false)  
                         [200
                          {}
                          body])
    (= "DELETE" method) (do
                          (io/delete-file path)
                          [200])
    (and (file/not-found? path)
         (= "GET" method)) [200]
    (= "GET" method) [200
                      {}
                      (response/content path)]))

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

(defn check-routes [method uri query headers body path]
  (cond 
    (route? method uri) ((static-routes method) uri)
    (parameters? method uri) [200
                              {}
                              (format-query query)]
    (= "/form" uri) (handle-form method
                                 path
                                 body)
    :else nil))

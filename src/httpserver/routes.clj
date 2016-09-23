(ns httpserver.routes
  (:import [java.util Base64])
  (:require [clojure.string :as string]
            [clojure.java.io :as io]))

(def static-routes
  {"GET" {"/redirect" 
          [302 {"Location" "http://localhost:5000/"}]
          "/coffee" 
          [418 {} "I'm a teapot"]
          "/tea" [200] 
         }
   "PUT" {"/form" [200]
          "/file1" [405]} 
   "POST" {"/form" [200]
           "/text-file.txt" [405]}
   "OPTIONS" {"/method_options2" 
              [200 {"Allow" "GET,OPTIONS"}]
              "/method_options" 
              [200 {"Allow" "GET,HEAD,POST,OPTIONS,PUT"}]}
}) 

(defn parameters? [method uri]
  (and (= method "GET")
       (= uri "/parameters")))

(defn restricted? [method uri]
  (and (= method "GET")
       (= uri "/logs")))

(defn decode-base64 [encoded-string]
  (let [decoder (Base64/getDecoder)]
    (string/join "" 
                 (map char 
                      (.decode decoder encoded-string)))))

(defn encode-base64 [decoded-string]
  (let [encoder (Base64/getEncoder)]
    (.encodeToString encoder  
                     (byte-array (map (comp byte int)
                                      decoded-string)))))

(defn authentication [headers]
  (if (and (contains? headers "Authorization")
           (= (headers "Authorization")
              (str "Basic "
                   (encode-base64 "admin:hunter2")))) [200]
    [401 {"WWW-Authenticate"
          "Basic realm=\"Admin\""}]))

(defn format-query [query]
  (if (string? query) query 
    (string/join "\r\n" (map #(str % " = " (query %))
                             (keys query)))))

(defn check-routes [method uri query headers]
  (cond 
    (and (contains? static-routes method)
         (contains? (static-routes method) 
                    uri)) ((static-routes method) uri)
    (parameters? method uri) [200
                              {}
                              (format-query query)]
    (restricted? method uri) (authentication headers) 
    :else nil))

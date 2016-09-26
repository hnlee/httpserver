(ns httpserver.request
  (:require [clojure.string :as string]))


(defn parse-request-line [request-line]
  (let [[all method uri version]
        (re-find #"^([A-Z]+) (.+) (HTTP.+)" 
                 request-line)]
    {:method method
     :uri uri}))

(defn parse-headers [headers]
  (if-not 
    (= "" headers) (as-> headers lines
                         (string/split lines #"\r\n")
                         (map #(string/split % #": ")
                              lines) 
                         (apply concat lines)
                         (apply hash-map lines))
    {})) 
        
(defn parse [msg]
  (let [[all request-line headers body]
        (re-find #"(?s)(.+?)\r\n(.*)\r\n\r\n(.*)" msg)]
    (merge (parse-request-line request-line)
           {:headers (parse-headers headers)})))

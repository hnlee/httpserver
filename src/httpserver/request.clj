(ns httpserver.request
  (:require [clojure.string :as string]))


(defn parse-status-line [status-line]
  (let [[all method uri version]
        (re-find #"^([A-Z]+) (.+) (HTTP.+)\r\n" 
                 status-line)]
    {:method method
     :uri uri}))

(defn parse-headers [headers]
  (if-not 
    (= "" headers) (as-> headers lines
                         (string/split lines #"\r\n")
                         (map #(string/split % #": *")
                              lines) 
                         (apply concat lines)
                         (apply hash-map lines))
    "")) 
        
(defn parse [msg]
  (let [[all status-line headers body]
        (re-find #"^(.+?\r\n)(.*)\r\n(.*)$" msg)]
    (merge (parse-status-line status-line)
           {:headers (parse-headers headers)})))

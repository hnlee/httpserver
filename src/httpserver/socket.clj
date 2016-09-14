(ns httpserver.socket
  (:import (java.net ServerSocket))
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

(defn open [port]
  (ServerSocket. port))

(defn close [opened-socket]
  (.close opened-socket))

(defn listen [server]
  (.accept server))

(defn body? [head]
  ((complement nil?) (string/index-of head 
                                      "Content-Length")))

(defn read-head [reader]
  (loop [head ""
         line (.readLine reader)]
    (if (or (= "" line) (nil? line)) head
      (recur (str head line "\r\n")
             (.readLine reader))))) 

(defn read-body [head reader]
  (let [length (Integer. ((re-find #"Content-Length: (\d+)"
                                   head) 1))]
    (apply str 
           (for [n (range length)] (char (.read reader))))))

(defn receive [connected-socket]
  (let [reader (io/reader connected-socket)]
    (hash-map :request-line (.readLine reader))))

(defn give [connected-socket response]
  (let [writer (io/writer connected-socket)]
    (.write writer response) 
    (.flush writer))) 

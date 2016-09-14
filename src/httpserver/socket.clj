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

(defn body? [msg]
  ((complement nil?) (string/index-of msg "Content-Length")))

(defn read-headers [reader]
  (loop [headers ""
         line (.readLine reader)]
    (if (or (= "" line) (nil? line)) headers
      (recur (str headers line "\r\n")
             (.readLine reader))))) 

(defn receive [connected-socket]
  (let [reader (io/reader connected-socket)]
    (hash-map :request-line (.readLine reader))))

(defn give [connected-socket response]
  (let [writer (io/writer connected-socket)]
    (.write writer response) 
    (.flush writer))) 

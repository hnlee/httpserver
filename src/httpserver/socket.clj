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

(defn receive [connected-socket]
  (let [reader (io/reader connected-socket)]
    (hash-map :request-line (.readLine reader))))

(defn give [connected-socket response]
  (let [writer (io/writer connected-socket)]
    (.write writer response) 
    (.flush writer))) 

(ns httpserver.socket
  (:import (java.net ServerSocket))
  (:require [clojure.java.io :as io]))

(defn open [port]
  (ServerSocket. port))

(defn close [opened-socket]
  (.close opened-socket))

(defn listen [server]
  (.accept server))

(defn receive [connected-socket]
  (.readLine (io/reader connected-socket)))

(defn give [connected-socket response]
  (let [writer (io/writer connected-socket)]
    (.write writer response) 
    (.flush writer))) 

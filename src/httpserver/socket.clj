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
  (let [[all length]
        (re-find #"[Cc]ontent-[Ll]ength: ?(\d+)" head)]
    (apply str (for [n (range (Integer. length))] 
                 (char (.read reader))))))

(defn receive [connection]
  (let [reader (io/reader connection)
        head (read-head reader)]
    (if (body? head) (str head 
                          "\r\n" 
                          (read-body head reader))
      (str head "\r\n"))))

(defn give [connection response]
  (let [stream (io/output-stream connection)]
    (.write stream 
            (byte-array response)
            0
            (count response)) 
    (.flush stream))) 

(ns httpserver.socket
  (:import (java.net ServerSocket))
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

(def crlf "\r\n")

(defn open [port]
  (ServerSocket. port))

(defn close [opened-socket]
  (.close opened-socket))

(defn listen [server]
  (.accept server))

(defn body? [headers]
  (string/includes? headers "Content-Length"))

(defn read-request-line [reader]
  (str (.readLine reader) crlf))

(defn read-headers [reader]
  (loop [headers ""
         line (.readLine reader)]
    (cond 
      (and (= "" line) (= "" headers)) crlf
      (= "" line) headers 
      :else (recur (str headers line crlf)
                   (.readLine reader))))) 

(defn read-body [headers reader]
  (let [[all length]
        (re-find #"[Cc]ontent-[Ll]ength: ?(\d+)" headers)]
    (apply str (for [n (range (Integer. length))] 
                 (char (.read reader))))))

(defn receive [connection]
  (let [reader (io/reader connection)
        request-line (read-request-line reader)
        headers (read-headers reader)]
    (if (body? headers) (str request-line 
                             headers
                             crlf
                             (read-body headers reader))
      (str request-line headers crlf))))

(defn give [connection response]
  (let [stream (io/output-stream connection)]
    (.write stream 
            (byte-array response)
            0
            (count response)) 
    (.flush stream))) 

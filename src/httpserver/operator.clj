(ns httpserver.operator
  (:require [clojure.java.shell :as shell] 
            [clojure.string :as string]
            [clojure.java.io :as io]
            [httpserver.socket :as socket]
            [httpserver.request :as request]
            [httpserver.response :as response]
            [httpserver.router :as router]))

(defn not-found? [path]
  (not (.exists (io/as-file path)))) 

(defn directory? [path]
  (.isDirectory (io/as-file path)))

(defn ls [path]
  (apply list (.list (io/as-file path))))

(defn linkify [paths]
  (string/join (map (fn [path] (str "<a href=\"/"
                                    path
                                    "\">"
                                    path
                                    "</a><br />")) paths))) 

(defn htmlify [title body]
  (str "<html><head><title>"
       title
       "</title></head><body>"
       body
       "</body></html>"))

(defn content [path]
  (if (directory? path) (htmlify (str "Index of "
                                      ((re-find #".*(/.*?)$"
                                                path) 1))
                                 (linkify (ls path)))
    (let [file (io/as-file path)]
      (with-open [stream (io/input-stream file)] 
        (vec (repeatedly (.length file) 
                         #(.read stream)))))))

(defn content-type [path]
  (cond 
    (directory? path) "text/html"
    ((complement nil?) 
      (re-find #"(?i)\.jpe{0,1}g$" path)) "image/jpeg" 
    :else
    "text/plain"))

(defn choose-response [client-request dir]
  (let [msg (request/parse client-request)
        method (msg :method)
        uri (msg :uri)
        path (str dir (msg :uri))]
    (cond
      (and (contains? router/routes 
                      method)
           (contains? (router/routes method) 
                      uri)) (apply 
                              response/compose
                              ((router/routes method) uri)) 
      (not-found? path) (response/compose 404)
      (= method "HEAD") (response/compose 200)
      (= method "GET") (response/compose 
                         200
                         {"Content-Type" (content-type path)}
                         (content path)))
))
 
(defn serve [connection dir]
  (let [client-request (socket/receive connection)
        server-response (choose-response client-request
                                         dir)]
    (socket/give connection server-response)))


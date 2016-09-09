(ns httpserver.operator)

(defn serve [port dir]
)

(comment 
(ns httpserver.operator
  (:require [httpserver.socket :as socket]
            [httpserver.request :as request]
            [httpserver.response :as response]))

(defn get? [request-line]
  (= "GET" ((request/parse request-line) :method)))

(defn serve [port dir]
  (with-open
    [server (socket/open port)
     client (socket/listen server)
     reader (socket/reader client)
     writer (socket/writer client)]
    (let [request-line (socket/receive reader)]
      (if (get? request-line)
        (socket/give (response/compose 200) writer))
    )))
    )

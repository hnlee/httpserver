(ns httpserver.operator
  (:require [clojure.java.shell :as shell] 
            [clojure.string :as string]
            [clojure.java.io :as io]
            [httpserver.socket :as socket]
            [httpserver.request :as request]
            [httpserver.response :as response]))

(defn not-found? [uri dir]
  (let [path (str "." uri)]
    (not (.exists (io/as-file path))))) 

(defn choose-response [client-request dir]
  (let [parsed-request (request/parse client-request)]
    (if (and (contains? #{"HEAD" "GET"}
                        (parsed-request :method))
             (not-found? (parsed-request :uri) 
                         dir)) (response/compose 404)
      (response/compose 200))))
 
(defn serve [connection dir]
  (let [client-request (socket/receive connection)
        server-response (choose-response client-request
                                         dir)]
    (socket/give connection server-response)))


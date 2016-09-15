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
  (let [msg (request/parse client-request)]
    (cond 
      (= "/coffee" (msg :uri)) (response/compose 418)
      (and (= "/method_options2" 
              (msg :uri))) (response/compose
                             200
                             {"Allow" "GET,OPTIONS"})
      (and (contains? #{"HEAD" "GET"} (msg :method))
           (not-found? (msg :uri) dir)) (response/compose 404)
      (= "OPTIONS"
         (msg :method)) (response/compose 
                          200
                          {"Allow" 
                           "GET,HEAD,POST,OPTIONS,PUT"})
      :else (response/compose 200))))
 
(defn serve [connection dir]
  (let [client-request (socket/receive connection)
        server-response (choose-response client-request
                                         dir)]
    (socket/give connection server-response)))


(ns httpserver.operator
  (:require [clojure.java.shell :as shell] 
            [clojure.string :as string]
            [clojure.java.io :as io]
            [httpserver.socket :as socket]
            [httpserver.request :as request]
            [httpserver.response :as response]))

(defn not-found? [uri dir]
  (let [path (str dir uri)]
    (not (.exists (io/as-file path))))) 

(defn directory? [uri dir]
  (let [path (str dir uri)]
    (.isDirectory (io/as-file path))))

(defn ls [uri dir]
  (let [path (str dir uri)]
    (apply list (.list (io/as-file path)))))

(defn choose-response [client-request dir]
  (let [msg (request/parse client-request)]
    (cond 
      (= "/redirect"
         (msg :uri)) (response/compose 302
                                       {"Location"
                                        "http://localhost:5000/"})
      (= "/coffee" 
         (msg :uri)) (response/compose 418
                                       {}
                                       "I'm a teapot")
      (= "/tea"
         (msg :uri)) (response/compose 200)
      (= "/method_options2" 
         (msg :uri)) (response/compose 200
                                       {"Allow" 
                                        "GET,OPTIONS"})
      (and (contains? #{"HEAD" "GET"} (msg :method))
           (not-found? 
             (msg :uri) dir)) (response/compose 404)
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


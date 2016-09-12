(ns httpserver.operator
  (:require [clojure.java.shell :as shell] 
            [clojure.string :as string]
            [httpserver.socket :as socket]
            [httpserver.request :as request]
            [httpserver.response :as response]))

(defn not-found? [uri dir]
  (let [test-cmd "test -e %s && echo 1 || echo 0"
        path (str dir uri)]
    (= "0\n" ((shell/sh "sh" "-c" (format test-cmd
                                          path)) :out))))

(defn choose-response [client-request dir]
  (let [parsed-request (request/parse client-request)]
    (if (and (contains? #{"GET" "HEAD"}
                        (parsed-request :method))
             (not-found? (parsed-request :uri) 
                         dir)) (response/compose 404)
      (response/compose 200))))
 

(defn serve [connection dir]
  (let [client-request (socket/receive connection)
        server-response (choose-response client-request
                                         dir)]
    (socket/give connection server-response)))


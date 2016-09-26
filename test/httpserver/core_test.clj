(ns httpserver.core-test
  (:import (java.net Socket))
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [httpserver.socket :as socket]
            [httpserver.core :refer :all])) 

(deftest test-set-vars 
  (testing "Use default settings if no flags"
    (is (= (hash-map :port 5000 :dir default-dir)
           (set-vars '()))))
  (testing "Set port when only dir is given"
    (is (= (hash-map :port 8888 :dir default-dir)
           (set-vars '("-p" "8888")))))
  (testing "Set dir when only port is given"
    (is (= (hash-map :port 5000 :dir "~")
           (set-vars '("-d" "~")))))
  (testing "Use given settings if both flags"
    (is (= (hash-map :port 8888 :dir "~")
           (set-vars '("-p" "8888" "-d" "~")))))
)

(deftest test-serve
  (with-open [server (socket/open 5000)
              client-socket (Socket. "localhost" 5000)
              client-out (io/writer client-socket)
              client-in (io/reader client-socket)
              connection (socket/listen server)]
    (testing "Server sends response to request"
      (.write client-out (str "HEAD /foobar HTTP/1.1\r\n"
                              "\r\n"
                              "\r\n"))
      (.flush client-out)
      (serve connection ".")
      (is (= "HTTP/1.1 404 Not found"
             (.readLine client-in))))
))

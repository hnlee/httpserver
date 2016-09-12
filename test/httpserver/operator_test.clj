(ns httpserver.operator-test
  (:import (java.net Socket))
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [httpserver.operator :refer :all]
            [httpserver.socket :as socket]))

(deftest test-get?
  (testing "Detect GET"
    (let [request-line "GET / HTTP/1.1\r\n"]
      (is get? request-line))))


(deftest test-choose-response
  (testing "GET request returns 200 response"
    (let [request-line "GET / HTTP/1.1\r\n"
          status-line "HTTP/1.1 200 OK\r\n"]
      (is (= status-line (choose-response request-line)))))
) 

(deftest test-serve
  (with-open [server (socket/open 5000)
              client-socket (Socket. "localhost" 5000)
              client-out (io/writer client-socket)
              client-in (io/reader client-socket)
              connection (socket/listen server)]
    (testing "Server sends 200 response to GET request"
      (let [request-line "GET / HTTP/1.1\r\n"
            status-line "HTTP/1.1 200 OK\r\n"]
        (.write client-out request-line)
        (.flush client-out)
        (serve connection)
        (is (= (str/trim-newline status-line) 
               (.readLine client-in)))))
))

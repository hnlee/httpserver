(ns httpserver.operator-test
  (:import (java.net Socket))
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [httpserver.operator :refer :all]
            [httpserver.socket :as socket]))

(def get-request "GET / HTTP/1.1\r\n")

(def put-request (str "PUT /form HTTP/1.1\r\n"
                      "\r\n"
                      "My=Data"))

(def head-request "HEAD /foobar HTTP/1.1\r\n")

(def get-request-line 
  (hash-map :request-line 
            (string/trim-newline get-request))) 

(def put-request-line 
  (hash-map :request-line 
            ((string/split-lines put-request) 0))) 

(def head-request-line
  (hash-map :request-line
            (string/trim-newline head-request)))


(def response-200 "HTTP/1.1 200 OK\r\n")

(def response-404 "HTTP/1.1 404 Not found\r\n")

(deftest test-choose-response
  (testing "GET request returns 200 response"
    (is (= response-200 
           (choose-response get-request-line "."))))
  (testing "PUT request returns 200 response"
    (is (= response-200 
           (choose-response put-request-line "."))))
  (testing "HEAD request with invalid URI returns 404 response"
    (is (= response-404
           (choose-response head-request-line ".")))) 
) 

(deftest test-serve
  (with-open [server (socket/open 5000)
              client-socket (Socket. "localhost" 5000)
              client-out (io/writer client-socket)
              client-in (io/reader client-socket)
              connection (socket/listen server)]
    (testing "Server sends 200 response to GET request"
      (.write client-out get-request)
      (.flush client-out)
      (serve connection ".")
      (is (= (string/trim-newline response-200)
             (.readLine client-in)))) 
    (testing "Server sends 200 response to PUT request"
      (.write client-out put-request)
      (.flush client-out)
      (serve connection ".")
      (is (= (string/trim-newline response-200)
             (.readLine client-in))))
    (testing "Server sends 404 respone to HEAD request with invalid response"
      (.write client-out head-request)
      (.flush client-out)
      (serve connection ".")
      (is (= (string/trim-newline response-404)
             (.readLine client-in))))

))

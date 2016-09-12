(ns httpserver.operator-test
  (:import (java.net Socket))
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [httpserver.operator :refer :all]
            [httpserver.socket :as socket]))

(def get-request "GET / HTTP/1.1\r\n")

(def put-request (str "PUT /form HTTP/1.1\r\n"
                      "\r\n"
                      "My=Data"))

(def post-request (str "POST /form HTTP/1.1\r\n"
                       "\r\n"
                       "My=Data"))

(def response-200 "HTTP/1.1 200 OK\r\n")

(def response-404 "HTTP/1.1 404 Not found\r\n")

(deftest test-choose-response
  (testing "GET request returns 200 response"
    (is (= response-200 (choose-response get-request))))
  (testing "PUT request returns 200 response"
    (is (= response-200 (choose-response put-request)))) 
  (testing "POST request returns 200 response"
    (is (= response-200 (choose-response post-request))))
) 

(deftest test-serve
  (with-open [server (socket/open 5000)
              client-socket (Socket. "localhost" 5000)
              client-out (io/writer client-socket)
              client-in (io/reader client-socket)
              connection (socket/listen server)]
    (testing "Server sends 200 response to GET request"
      (do 
        (.write client-out get-request)
        (.flush client-out)
        (serve connection)
        (is (= (str/trim-newline response-200)
               (.readLine client-in))))) 
    (testing "Server sends 200 response to PUT request"
      (do
        (.write client-out put-request)
        (.flush client-out)
        (serve connection)
        (is (= (str/trim-newline response-200)
               (.readLine client-in)))))
    (testing "Server sends 200 response to POST request"
      (do 
        (.write client-out post-request)
        (.flush client-out)
        (serve connection)
        (is (= (str/trim-newline response-200)
               (.readLine client-in)))))
))

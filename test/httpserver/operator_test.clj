(ns httpserver.operator-test
  (:import (java.net Socket))
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [httpserver.operator :refer :all]
            [httpserver.socket :as socket]))

(def request-string (str "%s %s HTTP/1.1\r\n"
                         "%s\r\n"
                         "\r\n"
                         "%s\r\n"))

(def response-string (str "HTTP/1.1 %d %s\r\n"))

(def simple-get-request (format request-string
                                "GET" "/" "" ""))

(def simple-put-request (format request-string
                                "PUT" "/form" "" "My=Data"))

(def invalid-head-request (format request-string
                                  "HEAD" "/foobar" "" ""))

(def all-options-request (format request-string
                                 "OPTIONS" 
                                 "/method_options" 
                                 "" ""))

(def some-options-request (format request-string
                                  "OPTIONS" 
                                  "/method_options2" 
                                  "" ""))

(def coffee-get-request (format request-string
                                "GET"
                                "/coffee"
                                "" ""))

(def response-200 (format response-string
                                 200 "OK"))

(def response-404 (format response-string
                          404 "Not found"))

(def response-418 (format response-string
                          418 "I'm a teapot"))

(deftest test-choose-response
  (testing "GET request returns 200 response"
    (is (= response-200 
           (choose-response simple-get-request "."))))
  (testing "PUT request returns 200 response"
    (is (= response-200 
           (choose-response simple-put-request "."))))
  (testing "HEAD request with invalid URI returns 404 response"
    (is (= response-404
           (choose-response invalid-head-request ".")))) 
  (testing "OPTIONS request returns all methods" 
    (is (= (str response-200
                "Allow: GET,HEAD,POST,OPTIONS,PUT"
                "\r\n\r\n")
           (choose-response all-options-request ".")))) 
  (testing "OPTIONS request returns some methods"
    (is (= (str response-200
                "Allow: GET,OPTIONS"
                "\r\n\r\n")
           (choose-response some-options-request "."))))
  (testing "GET /coffee returns 418 response"
    (is (= response-418
           (choose-response coffee-get-request ".")))) 

)

(defn read-response [reader]
  (loop [msg ""
         line (.readLine reader)]
    (if (= "" line) (str msg "\r\n")
      (recur (str msg line "\r\n") (.readLine reader)))))

(deftest test-serve
  (with-open [server (socket/open 5000)
              client-socket (Socket. "localhost" 5000)
              client-out (io/writer client-socket)
              client-in (io/reader client-socket)
              connection (socket/listen server)]
    (testing "Server sends 200 response to GET request"
      (.write client-out simple-get-request)
      (.flush client-out)
      (serve connection ".")
      (is (= (string/trim-newline response-200)
             (.readLine client-in)))) 
    (testing "Server sends 200 response to PUT request"
      (.write client-out simple-put-request)
      (.flush client-out)
      (serve connection ".")
      (is (= (string/trimr response-200)
             (.readLine client-in))))
    (testing "Server sends 404 response to HEAD request with invalid URI"
      (.write client-out invalid-head-request)
      (.flush client-out)
      (serve connection ".")
      (is (= (string/trimr response-404)
             (.readLine client-in))))
    (testing "Server sends 200 response with Allow header to OPTIONS request with all methods"
      (.write client-out all-options-request)
      (.flush client-out)
      (serve connection ".")
      (is (= (str response-200
                  "Allow: GET,HEAD,POST,OPTIONS,PUT"
                  "\r\n\r\n")
             (read-response client-in)))) 
    (testing "Server sends 200 response with Allow header to OPTIONS request with only some methods"
      (.write client-out some-options-request)
      (.flush client-out)
      (serve connection ".")
      (is (= (str response-200
                  "Allow: GET,OPTIONS"
                  "\r\n\r\n")
             (read-response client-in)))) 
))

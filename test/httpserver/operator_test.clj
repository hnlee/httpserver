(ns httpserver.operator-test
  (:import (java.net Socket))
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [httpserver.operator :refer :all]
            [httpserver.response :as response]
            [httpserver.socket :as socket]))

(def request-string (str "%s %s HTTP/1.1\r\n"
                         "%s\r\n"
                         "\r\n"
                         "%s\r\n"))

(def response-string (str "HTTP/1.1 %d %s\r\n"))

(def dir-get-request (format request-string
                             "GET" "/" "" ""))

(def invalid-head-request (format request-string
                                  "HEAD" "/foobar" "" ""))

(def valid-head-request (format request-string
                                "HEAD" "/" "" ""))

(def coffee-get-request (format request-string
                                "GET"
                                "/coffee"
                                "" ""))

(def text-get-request (format request-string
                              "GET"
                              "/test/httpserver/public/file1"
                              "" ""))

(def encoded-tea-request (format request-string
                                 "GET"
                                 "/%74%65%61"
                                 "" ""))

(def query-request (format request-string
                           "GET"
                           "/form?my=data&your=data"
                           "" ""))

(def response-200 (format response-string
                                 200 "OK"))

(def response-404 (format response-string
                          404 "Not found"))

(def response-418 (format response-string
                          418 "I'm a teapot"))

(deftest test-not-found? 
  (testing "File that exists"
    (is (not (not-found? "./project.clj"))))
  (testing "File that does not exist"
    (is (not-found? "./nonsense")))
  (testing "Directory that exists"
    (is (not (not-found? "./src")))))

(deftest test-parse-query
  (testing "URI without query"
    (is (nil? (parse-query "/form"))))
  (testing "URI with query"
    (is (= "my = data"
           (parse-query "/form?my=data"))))
  (testing "URI with multiple variables in query"
    (is (= "my = data\r\nyour = data"
           (parse-query "/form?my=data&your=data")))))
 
(deftest test-decode-uri
  (testing "Decode URL-encoded characters in URI"
    (is (= " "
           (decode-uri "%20")))
    (is (= " <, >"
           (decode-uri "%20%3C%2C%20%3E")))))
  
(deftest test-choose-response
  (testing "Invalid URI returns 404 response"
    (is (= (response/compose 404) 
           (choose-response invalid-head-request ".")))) 
  (testing "Able to get hard-coded route from hashmap"
    (is (= (response/compose 418
                             {}
                             "I'm a teapot")
           (choose-response coffee-get-request ".")))) 
  (testing "HEAD on valid URI returns 200 response with no body" 
    (is (= (response/compose 200)
            (choose-response valid-head-request "."))))
  (testing "GET on text file returns content in body"
    (is (= (response/compose 200
                              {"Content-Type" "text/plain"}
                              "file1 contents")
            (choose-response text-get-request "."))))
  (testing "URI with encoded characters is decoded"
    (is (= (response/compose 200)
           (choose-response encoded-tea-request ".")))) 
  (testing "URI with query string returns variables"
    (is (= (response/compose 200
                             {"Content-Type" "text/plain"}
                             "my = data\r\nyour = data")
           (choose-response query-request ".")))) 
)

(deftest test-serve
  (with-open [server (socket/open 5000)
              client-socket (Socket. "localhost" 5000)
              client-out (io/writer client-socket)
              client-in (io/reader client-socket)
              connection (socket/listen server)]
    (testing "Server sends response to request"
      (.write client-out invalid-head-request)
      (.flush client-out)
      (serve connection ".")
      (is (= (string/trim-newline response-404)
             (.readLine client-in))))

))

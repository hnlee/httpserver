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

(deftest test-directory?
  (testing "Path to a directory"
    (is (directory? "./src")))
  (testing "Path to a file"
    (is (not (directory? "./project.clj")))))

(deftest test-ls
  (testing "Get contents of a directory"
    (is (= (apply list (.list (io/as-file "./")))
           (ls "./")))))

(deftest test-linkify
  (testing "Return HTML markup for list of paths" 
    (is (= (str "<a href=\"/file1\">file1</a><br />"
                "<a href=\"/file2\">file2</a><br />")
           (linkify '("file1" "file2"))))))

(deftest test-htmlify
  (testing "Return HTML document given title and body"
    (is (= (str "<html><head>"
                "<title>Hello World</title>"
                "</head><body>Hello world!</body></html>")
           (htmlify "Hello World" "Hello world!")))))

(deftest test-content
  (testing "Return text file content"
    (let [path "test/httpserver/public/file1"]
      (is (= (map (comp byte int) "file1 contents") 
             (content path)))))
  (testing "Return image file content"
    (let [path "test/httpserver/public/image.jpeg"
          file (io/as-file path)]
      (with-open [stream (io/input-stream file)]
        (is (= (vec (repeatedly (.length file)
                                #(.read stream))) 
               (content path))))))
)

(deftest test-content-type
  (testing "Plain text content"
    (let [path "test/httpserver/public/file1"]
      (is (= "text/plain"
             (content-type path)))))
  (testing "Image content"
    (let [path "test/httpserver/public/image.jpeg"]
      (is (= "image/jpeg"
             (content-type path)))))
)
     
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

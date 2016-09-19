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

(def tea-get-request (format request-string
                             "GET"
                             "/tea"
                             "" ""))

(def redirect-get-request (format request-string
                                  "GET"
                                  "/redirect"
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

(def response-302 (format response-string
                          302 "Found"))

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
      (is (= "file1 contents"
             (content path))))))
     
(deftest test-choose-response
  (testing "GET request returns 200 response"
    (is ((complement nil?) 
         (re-find 
           (re-pattern response-200) 
           (choose-response simple-get-request ".")))))
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
    (is (= (str response-418
                "Content-Length: 12"
                "\r\n\r\n"
                "I'm a teapot")
           (choose-response coffee-get-request ".")))) 
  (testing "GET /tea returns 200 response"
    (is (= response-200
           (choose-response tea-get-request "."))))
  (testing "GET /redirect returns 302 response"
    (is (= (str response-302
                "Location: http://localhost:5000/"
                "\r\n\r\n")
           (choose-response redirect-get-request ".")))) 
  (testing "GET / returns directory listing with links"
    (let [html (htmlify "Index of /" (linkify (ls "./")))]
      (is (= (str response-200
                  "Content-Length: "
                  (count html)
                  "\r\n\r\n"
                  html)
             (choose-response simple-get-request ".")))))
  (testing "GET on text file returns content in body"
    (is (= (str response-200
                "Content-Length: 14"
                "\r\n\r\n" 
                "file1 contents")
           (choose-response text-get-request "."))))
)

(deftest test-serve
  (with-open [server (socket/open 5000)
              client-socket (Socket. "localhost" 5000)
              client-out (io/writer client-socket)
              client-in (io/reader client-socket)
              connection (socket/listen server)]
    (testing "Server sends 404 response to HEAD request"
      (.write client-out invalid-head-request)
      (.flush client-out)
      (serve connection ".")
      (is (= (string/trim-newline response-404)
             (.readLine client-in))))

))

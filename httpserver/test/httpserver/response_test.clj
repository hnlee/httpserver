(ns httpserver.response-test
  (:import (java.io File))
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [httpserver.encoding :as code]
            [httpserver.http-messages :refer :all]
            [httpserver.response :refer :all]))

(deftest test-ls
  (testing "Get contents of a directory"
    (let [test-dir (proxy [File] ["test/path"]
                     (list [] (into-array String 
                                          ["file1" 
                                           "file2"])))] 
      (is (= '("file1" "file2") 
             (ls test-dir))))))

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

(def text-path (str test-path "/file1"))

(def image-path (str test-path "/image.jpeg"))

(deftest test-content
  (testing "Return text file content"
    (is (= (code/str->bytes "file1 contents") 
           (content text-path))))
  (testing "Return image file content"
    (let [file (io/as-file image-path)]
      (with-open [stream (io/input-stream file)]
        (is (= (vec (repeatedly (.length file)
                                #(.read stream))) 
               (content image-path))))))
  (let [partial-path (str test-path 
                          "/partial_content.txt")]
    (testing "Return partial file content with both indices"
      (is (= (subvec (content partial-path) 0 5)
             (content partial-path 0 4))))
    (testing "Return partial file content with only start index"
      (is (= (subvec (content partial-path) 4)
             (content partial-path 4 nil))))
    (testing "Return partial file content with only end index"
      (is (= (vec (take-last 6 (content partial-path)))
             (content partial-path nil 6))))))

(deftest test-content-type
  (testing "Plain text content"
    (is (= "text/plain"
           (content-type text-path))))
  (testing "Image content"
    (is (= "image/jpeg"
           (content-type image-path)))))

(deftest test-format-status-line
  (testing "Format 200 status line"
    (= "HTTP/1.1 200 OK"
       (format-status-line 200))))

(deftest test-format-headers
  (testing "Format headers hashmap into string"
    (= "Allow: GET\r\nContent-Length: 10"
       (format-headers {"Allow" "GET" 
                        "Content-Length" 10}))))

(deftest test-compose
  (testing "Return 200 status code"
    (is (= (code/str->bytes 
             (format response-string 200 "OK"))
           (compose 200))))
  (testing "Return 404 status code"
    (is (= (code/str->bytes 
             (format response-string 404 "Not found"))
           (compose 404))))
  (testing "Return 200 status code with Allows header"
    (is (= (code/str->bytes 
             (str (format response-string 200 "OK")
                  "Allow: GET,HEAD,POST,OPTIONS,PUT"
                  "\r\n\r\n"))         
           (compose 200
                    {"Allow" "GET,HEAD,POST,OPTIONS,PUT"}))))
  (testing "Return 418 status code with Content-Length header and message body"
    (is (= (code/str->bytes 
             (str (format response-string 
                          418 
                          "I'm a teapot")
                  "Content-Length: 12\r\n"
                  "\r\n"
                  "I'm a teapot"))
           (compose 418
                    {}
                    "I'm a teapot")))))

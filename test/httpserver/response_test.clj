(ns httpserver.response-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [httpserver.response :refer :all]))

(def response-string (str "HTTP/1.1 %d %s\r\n"))

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
             (content-type path))))))

(deftest test-format-status-line
  (testing "Format 200 status line"
    (= "HTTP/1.1 200 OK"
       (format-status-line 200))))

(deftest test-format-headers
  (testing "Format headers hashmap into string"
    (= "Allow: GET\r\nContent-Length: 10"
       (format-headers {"Allow" "GET" 
                        "Content-Length" 10}))))

(deftest test-str->bytes
  (testing "Convert string to byte array"
    (= (map (comp byte int) (range 97 102))
       (str->bytes "abc"))))

(deftest test-compose
  (testing "Return 200 status code"
    (is (= (str->bytes 
             (format response-string 200 "OK"))
           (compose 200))))
  (testing "Return 404 status code"
    (is (= (str->bytes 
             (format response-string 404 "Not found"))
           (compose 404))))
  (testing "Return 200 status code with Allows header"
    (is (= (str->bytes 
             (str (format response-string 200 "OK")
                  "Allow: GET,HEAD,POST,OPTIONS,PUT"
                  "\r\n\r\n"))         
           (compose 200
                    {"Allow" "GET,HEAD,POST,OPTIONS,PUT"}))))
  (testing "Return 418 status code"
    (is (= (str->bytes 
             (str (format response-string 418 "I'm a teapot")
                  "Content-Length: 12\r\n"
                  "\r\n"
                  "I'm a teapot"))
           (compose 418
                    {}
                    "I'm a teapot"))))
  )

(ns httpserver.response-test
  (:require [clojure.test :refer :all]
            [httpserver.response :refer :all]))

(def response-string (str "HTTP/1.1 %d %s\r\n"))

(deftest test-format-status-line
  (testing "Format 200 status line"
    (= "HTTP/1.1 200 OK"
       (format-status-line 200))))

(deftest test-format-headers
  (testing "Format headers hashmap into string"
    (= "Allow: GET\r\nContent-Length: 10"
       (format-headers {"Allow" "GET" 
                        "Content-Length" 10}))))

(deftest test-string->bytes
  (testing "Convert string to byte array"
    (= (byte-array (map (comp byte int) (range 97 102)))
       (string->bytes "abc"))))

(deftest test-compose
  (testing "Return 200 status code"
    (is (= (format response-string 200 "OK")
           (compose 200))))
  (testing "Return 404 status code"
    (is (= (format response-string 404 "Not found")
           (compose 404))))
  (testing "Return 200 status code with Allows header"
    (is (= (str (format response-string 200 "OK")
                "Allow: GET,HEAD,POST,OPTIONS,PUT"
                "\r\n\r\n")         
           (compose 200
                    {"Allow" "GET,HEAD,POST,OPTIONS,PUT"}))))
  (testing "Return 418 status code"
    (is (= (str (format response-string 418 "I'm a teapot")
                "Content-Length: 12\r\n"
                "\r\n"
                "I'm a teapot")
           (compose 418
                    {}
                    "I'm a teapot"))))
  (testing "Return 302 response code"
    (is (= (str (format response-string 302 "Found")
                "Location: http://localhost:5000/\r\n"
                "\r\n")
           (compose 302
                    {"Location" "http://localhost:5000/"}))))
)

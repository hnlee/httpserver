(ns httpserver.request-test
  (:require [clojure.test :refer :all]
            [httpserver.request :refer :all]))

(deftest test-parse-status-line
  (testing "Parse method and URI from status line"
    (is (= {:method "GET"
            :uri "/"}
           (parse-status-line "GET / HTTP/1.1\r\n")))))

(deftest test-parse-headers
  (testing "Return empty string if no headers"
    (is (= "" 
           (parse-headers ""))))
  (testing "Parse header type and value from headers"
    (is (= {"Content-Type" "plain/text"
            "Content-Length" "9"}
           (parse-headers (str "Content-Type: plain/text"
                               "\r\n"
                               "Content-Length: 9")))))) 

(deftest test-parse
  (testing "Get method and URI from simple request"
    (let [msg (parse "GET / HTTP/1.1\r\n\r\n")]
      (is (= "GET" (msg :method)))
      (is (= "/" (msg :uri)))
      (is (= "" (msg :headers)))))
  (testing "Get headers from request with headers"
    (let [msg (parse (str "PUT / HTTP/1.1\r\n"
                          "Content-Length: 0\r\n"
                          "\r\n"))]
      (is (= "PUT" (msg :method)))
      (is (= "/" (msg :uri)))
      (is (= {"Content-Length" "0"} (msg :headers)))))
)


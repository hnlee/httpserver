(ns httpserver.request-test
  (:require [clojure.test :refer :all]
            [httpserver.request :refer :all]))

(deftest test-parse-parameters
  (testing "Query with no parameters"
    (is (= "data"
           (parse-parameters "data"))))
  (testing "Query with single parameter"
    (is (= {"my" "data"}
           (parse-parameters "my=data"))))
  (testing "Query with multiple parameters"
    (is (= {"my" "data"
            "your" "data"}
           (parse-parameters "my=data&your=data")))))

(deftest test-parse-query
  (testing "URI without query"
    (is (= {:uri "/form"
            :query ""}
           (parse-query "/form"))))
  (testing "URI with query"
    (is (= {:uri "/form" 
            :query (parse-parameters "my=data")} 
           (parse-query "/form?my=data"))))
  (testing "URI with multiple variables in query"
    (is (= {:uri "/form"
            :query (parse-parameters "my=data&your=data")}
           (parse-query "/form?my=data&your=data")))))
 
(deftest test-parse-request-line
  (testing "Parse method and URI from request line"
    (is (= {:method "GET"
            :uri "/"
            :query ""}
           (parse-request-line "GET / HTTP/1.1\r\n")))))

(deftest test-parse-headers
  (testing "Return empty string if no headers"
    (is (= {} 
           (parse-headers ""))))
  (testing "Parse header type and value from headers"
    (is (= {"Content-Type" "plain/text"
            "Content-Length" "9"}
           (parse-headers (str "Content-Type: plain/text"
                               "\r\n"
                               "Content-Length: 9")))))) 

(deftest test-parse
  (testing "Get method and URI from simple request"
    (let [msg (parse "GET / HTTP/1.1\r\n\r\n\r\n")]
      (is (= "GET" (msg :method)))
      (is (= "/" (msg :uri)))
      (is (= {} (msg :headers)))))
  (testing "Get header and body from request"
    (let [msg (parse (str "PUT / HTTP/1.1\r\n"
                          "Content-Length: 7\r\n"
                          "\r\n"
                          "my=data"))]
      (is (= "PUT" (msg :method)))
      (is (= "/" (msg :uri)))
      (is (= {"Content-Length" "7"} (msg :headers)))
      (is (= "my=data" (msg :body)))))
  (testing "Get multiple headers from request"
    (let [msg (parse (str "GET / HTTP/1.1\r\n"
                          "Host: localhost:5000\r\n"
                          "Accept-Language: en-US,en\r\n"
                          "\r\n"))]
      (is (= "GET" (msg :method)))
      (is (= "/" (msg :uri)))))
)


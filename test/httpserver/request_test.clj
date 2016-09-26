(ns httpserver.request-test
  (:require [clojure.test :refer :all]
            [httpserver.request :refer :all]))

(deftest test-parse-request-line
  (testing "Parse method and URI from request line"
    (is (= {:method "GET"
            :uri "/"}
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
  (testing "Get headers from request with headers"
    (let [msg (parse (str "PUT / HTTP/1.1\r\n"
                          "Content-Length: 0\r\n"
                          "\r\n"))]
      (is (= "PUT" (msg :method)))
      (is (= "/" (msg :uri)))
      (is (= {"Content-Length" "0"} (msg :headers)))))
  (testing "Really long request"
    (let [msg (parse "GET / HTTP/1.1\r\nHost: localhost:5000\r\nConnection: keep-alive\r\nCache-Control: max-age=0\r\nUpgrade-Insecure-Requests: 1\r\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\r\nAccept-Encoding: gzip, deflate, sdch\r\nAccept-Language: en-US,en;q=0.8\r\nCookie: textwrapon=false; wysiwyg=textarea\r\n\r\n")]
      (is (= "GET" (msg :method)))
      (is (= "/" (msg :uri)))))
)


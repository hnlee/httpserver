(ns httpserver.request-test
  (:require [clojure.test :refer :all]
            [httpserver.request :refer :all]))

(deftest test-parse
  (testing "Return hashmap from GET request"
    (let [msg (parse "GET / HTTP/1.1\r\n\r\n")]
      (is (= "GET" (msg :method))))) 
  (testing "Return hashmap from PUT request"
    (let [msg (parse (str "PUT / HTTP/1.1\r\n"
                                  "Content-Length: 9\r\n"
                                  "\r\n"
                                  "My=Data\r\n"))]
      (is (= "PUT" (msg :method))))) 
  (testing "Return hashmap from POST request"
    (let [msg (parse (str "POST / HTTP/1.1\r\n"
                                  "Content-Length: 9\r\n"
                                  "\r\n"
                                  "My=Data\r\n"))]
      (is (= "POST" (msg :method)))))
  (testing "Return hashmap from HEAD request"
    (let [msg (parse "HEAD /foobar HTTP/1.1\r\n")]
      (is (= "HEAD" (msg :method)))
      (is (= "/foobar" (msg :uri)))))
)

  


(ns httpserver.request-test
  (:require [clojure.test :refer :all]
            [httpserver.request :refer :all]))

(deftest test-parse
  (testing "Return hashmap with method from GET request"
    (let [request-message (parse "GET / HTTP/1.1\r\n")]
      (is (= "GET" (request-message :method))))) 
  (testing "Return hashmap with method from PUT request"
    (let [request-message (parse (str "PUT / HTTP/1.1\r\n"
                                      "\r\n"
                                      "My=Data"))]
      (is (= "PUT" (request-message :method))))) 
)

  


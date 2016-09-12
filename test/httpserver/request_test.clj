(ns httpserver.request-test
  (:require [clojure.test :refer :all]
            [httpserver.request :refer :all]))

(deftest test-parse
  (testing "Return hashmap with method from GET request"
    (let [request-msg (parse (hash-map :request-line 
                                       "GET / HTTP/1.1"))]
      (is (= "GET" (request-msg :method))))) 
  (testing "Return hashmap with method from PUT request"
    (let [request-msg (parse (hash-map :request-line
                                       "PUT / HTTP/1.1"
                                       :body
                                       "My=Data"))]
      (is (= "PUT" (request-msg :method))))) 
  (testing "Return hashmap with method from POST request"
    (let [request-msg (parse (hash-map :request-line
                                       "POST / HTTP/1.1"
                                       :body
                                       "My=Data"))]
      (is (= "POST" (request-msg :method)))))
)

  


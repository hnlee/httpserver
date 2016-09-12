(ns httpserver.request-test
  (:require [clojure.test :refer :all]
            [httpserver.request :refer :all]))

(deftest test-parse
  (testing "Return hashmap from GET request"
    (let [request-msg (parse (hash-map :request-line 
                                       "GET / HTTP/1.1"))]
      (is (= "GET" (request-msg :method))))) 
  (testing "Return hashmap from PUT request"
    (let [request-msg (parse (hash-map :request-line
                                       "PUT / HTTP/1.1"
                                       :body
                                       "My=Data"))]
      (is (= "PUT" (request-msg :method))))) 
  (testing "Return hashmap from POST request"
    (let [request-msg (parse (hash-map :request-line
                                       "POST / HTTP/1.1"
                                       :body
                                       "My=Data"))]
      (is (= "POST" (request-msg :method)))))
  (testing "Return hashmap from HEAD request"
    (let [request-msg 
          (parse (hash-map :request-line
                           "HEAD /foobar HTTP/1.1"))]
      (is (= "HEAD" (request-msg :method)))
      (is (= "/foobar" (request-msg :uri)))))
)

  


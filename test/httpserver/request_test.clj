(ns httpserver.request-test
  (:require [clojure.test :refer :all]
            [httpserver.request :refer :all]))

(deftest test-parse
  (testing "Return hashmap with method key"
    (is (= (hash-map :method "GET")
           (parse "GET / HTTP/1.1\r\n"))))
)

(ns httpserver.response-test
  (:require [clojure.test :refer :all]
            [httpserver.response :refer :all]))

(deftest test-compose
  (testing "Return 200 status code"
    (is (= "HTTP/1.1 200 OK\r\n")
        (compose 200)))
  (testing "Return 404 status code"
    (is (= "HTTP/1.1 404 Not found\r\n")
        (compose 404)))
)

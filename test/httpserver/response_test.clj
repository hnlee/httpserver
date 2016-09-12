(ns httpserver.response-test
  (:require [clojure.test :refer :all]
            [httpserver.response :refer :all]))

(deftest test-compose
  (testing "Return 200 status code"
    (is (= "HTTP/1.1 200 OK\r\nConnection: close\r\n\r\n")
        (compose 200)))
)

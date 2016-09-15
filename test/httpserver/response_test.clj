(ns httpserver.response-test
  (:require [clojure.test :refer :all]
            [httpserver.response :refer :all]))

(def response-string (str "HTTP/1.1 %d %s\r\n"))

(deftest test-compose
  (testing "Return 200 status code"
    (is (= (format response-string 200 "OK")
           (compose 200))))
  (testing "Return 404 status code"
    (is (= (format response-string 404 "Not found")
           (compose 404))))
  (testing "Return 200 status code with Allows header"
    (is (= (str (format response-string 200 "OK")
                "Allow: GET,HEAD,POST,OPTIONS,PUT"
                "\r\n\r\n")         
           (compose 200
                    {"Allow" "GET,HEAD,POST,OPTIONS,PUT"}))))
)

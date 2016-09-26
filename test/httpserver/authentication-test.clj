(ns httpserver.router-test
  (:import [java.util Base64])
  (:require [clojure.test :refer :all]
            [httpserver.router :refer :all]
            [httpserver.response :as response]))

(deftest test-encode-base64
  (testing "Encode empty string"
    (is (= ""
           (encode-base64 ""))))
  (testing "Encode a string"
    ; Example taken from HTTP spec on basic authorization
    (is (= "QWxhZGRpbjpvcGVuIHNlc2FtZQ=="
           (encode-base64 "Aladdin:open sesame")))))

(deftest test-decode-base64
  (testing "Decode empty string"
    (is (= ""
           (decode-base64 ""))))
  (testing "Decode base64 string"
    (let [encoded (encode-base64 "admin")] 
      (is (= "admin"
             (decode-base64 encoded))))))


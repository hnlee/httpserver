(ns httpserver.encoding-test
  (:import [java.util Base64])
  (:require [clojure.test :refer :all]
            [httpserver.encoding :refer :all]
            [httpserver.response :as response]))

(deftest test-decode-uri
  (testing "Decode non-encoded URL"
    (is (= "/form"
           (decode-uri "/form"))))
  (testing "Decode encoded character"
    (is (= " "
           (decode-uri "%20"))))
  (testing "Decode encoded character with hexadecimal digit"
    (is (= "<"
           (decode-uri "%3C"))))
  (testing "Decode multiple encoded characters" 
    (is (= " <, >"
           (decode-uri "%20%3C%2C%20%3E")))))

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


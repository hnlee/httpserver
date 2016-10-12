(ns httpserver.encoding-test
  (:import [java.util Base64])
  (:require [clojure.test :refer :all]
            [httpserver.response :as response]
            [httpserver.encoding :refer :all]))

(deftest test-str->bytes
  (testing "Convert string to byte array"
    (= (map (comp byte int) (range 97 102))
       (str->bytes "abc"))))

(deftest test-bytes->str
  (testing "Convert bytes to string"
    (is (= "hello"
           (bytes->str (str->bytes "hello"))))))

(deftest test-bytes->hex
  (testing "Convert single byte to hexstring"
    (is (= "0f"
           (bytes->hex (repeat 1 (byte 15))))))
  (testing "Convert seq of bytes to hexstring"
    (is (= "0f0f"
           (bytes->hex (repeat 2 (byte 15)))))))

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

(deftest test-encode-uri
  (testing "Encode alphanumeric URL"
    (is (= "/form"
           (encode-uri "/form"))))
  (testing "Encode space"
    (is (= "%20"
           (encode-uri " "))))
  (testing "Encode <"
    (is (= "%3C"
           (encode-uri "<"))))
  (testing "Encode multiple non-alphanumeric characters"
    (is (= "%20%3C%2C%20%3E"
           (encode-uri " <, >")))))

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

(deftest test-encode-sha1
  (testing "Encode SHA1 string"
    (is (= "dc50a0d27dda2eee9f65644cd7e4c9cf11de8bec"
           (encode-sha1 "default content")))))

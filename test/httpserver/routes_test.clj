(ns httpserver.routes-test
  (:import (java.util Base64))
  (:require [clojure.test :refer :all]
            [httpserver.routes :refer :all]))

(deftest test-parameters?
  (testing "Return true if GET for /parameters"
    (is (parameters? "GET" "/parameters")))
  (testing "Return false if not a GET request"
    (is (not (parameters? "PUT" "/parameters"))))
  (testing "Return false if not /parameters"
    (is (not (parameters? "GET" "/")))))

(deftest test-restricted?
  (testing "Return true if GET for /logs"
    (is (restricted? "GET" "/logs")))
  (testing "Return false if not a GET request"
    (is (not (restricted? "PUT" "/logs"))))
  (testing "Return false if not /logs"
    (is (not (restricted? "GET" "/")))))

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

(deftest test-authentication
  (testing "Return 401 if no authentication in header"
    (is (= [401 {"WWW-Authenticate"
                 "Basic realm=\"Admin\""}]
           (authentication {}))))
  (testing "Return 401 if incorrect authentication"
    (is (= [401 {"WWW-Authenticate"
                 "Basic realm=\"Admin\""}]
           (authentication {"Authorization"
                            "Basic random-string"})))) 
  (testing "Return 200 if correct authentication"
    (is (= [200]
           (authentication {"Authorization"
                            (str "Basic "
                                 (encode-base64 "admin:hunter2"))})))))

(deftest test-format-query
  (testing "Format query with no parameters"
    (is (= "data"
           (format-query "data"))))
  (testing "Format query with single parameter"
    (is (= "my = data"
           (format-query {"my" "data"}))))
  (testing "Format multiple parameters in query"
    (is (= "my = data\r\nyour = data"
           (format-query {"my" "data"
                          "your" "data"})))))


(deftest test-check-routes
  (testing "Hard-coded route"
    (is (= [200]
           (check-routes "GET" "/tea" "" {}))))
  (testing "Dynamic route for parameters"
    (is (= [200 {} "my = data\r\nyour = data"]
           (check-routes "GET" 
                         "/parameters" 
                         {"my" "data"
                          "your" "data"}
                         {}))))
  (testing "Requesting logs without authentication"
    (is (= [401 {"WWW-Authenticate"
                 "Basic realm=\"Admin\""}] 
           (check-routes "GET"
                         "/logs"
                         ""
                         {}))))  
  (testing "Not in defined route"
    (is (nil? (check-routes "GET" 
                            "/not_a_route"
                            ""
                            {})))))

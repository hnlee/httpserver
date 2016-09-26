(ns httpserver.routes-test
  (:require [clojure.test :refer :all]
            [httpserver.authentication :as auth]
            [httpserver.routes :refer :all]))

(deftest test-parameters?
  (testing "Return true if GET for /parameters"
    (is (parameters? "GET" "/parameters")))
  (testing "Return false if not a GET request"
    (is (not (parameters? "PUT" "/parameters"))))
  (testing "Return false if not /parameters"
    (is (not (parameters? "GET" "/")))))

(deftest test-restricted
  (testing "Return credentials if GET for /logs"
    (is (= "admin:hunter2"
           (auth/decode-base64 (restricted "GET" 
                                           "/logs")))))
  (testing "Return nil if not a GET request"
    (is (nil? (restricted "PUT" "/logs"))))
  (testing "Return nil if not a restricted URI"
    (is (nil? (restricted "GET" "/")))))

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

(deftest test-route?
  (testing "Not a route"
    (is (not (route? "GET" "/notaroute"))))
  (testing "Is a route"
    (is (route? "GET" "/tea"))))

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
 (testing "Not in defined route"
    (is (nil? (check-routes "GET" 
                            "/not_a_route"
                            ""
                            {})))))

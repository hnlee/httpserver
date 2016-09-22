(ns httpserver.router-test
  (:require [clojure.test :refer :all]
            [httpserver.router :refer :all]))

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
           (check-routes "GET" "/tea" ""))))
  (testing "Dynamic route for parameters"
    (is (= [200 {} "my = data\r\nyour = data"]
           (check-routes "GET" 
                         "/parameters" 
                         {"my" "data"
                          "your" "data"}))))
  (testing "Not in defined route"
    (is (nil? (check-routes "GET" 
                            "/not_a_route"
                            "")))))

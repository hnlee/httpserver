(ns httpserver.routes-test
  (:require [clojure.test :refer :all]
            [httpserver.encoding :as code]
            [httpserver.file :as file]
            [httpserver.response :as response]
            [httpserver.routes :refer :all]))

(deftest test-handle-form
  (testing "Return 200 and update /form content when POST"
    (is (= [200 {} "data=fatcat"]
           (handle-form "POST" 
                        "test/httpserver/public/form" 
                        "data=fatcat")))
    (is (= "data=fatcat"
           (slurp "test/httpserver/public/form"))))
 (testing "Return 200 and update /form content when PUT"
    (is (= [200 {} "data=heathcliff"]
           (handle-form "PUT" 
                        "test/httpserver/public/form" 
                        "data=heathcliff")))
    (is (= "data=heathcliff"
           (slurp "test/httpserver/public/form"))))
  (testing "Return 200 and delete /form content when DELETE"
    (is (= [200]
           (handle-form "DELETE" 
                        "test/httpserver/public/form" 
                        "")))
    (is (file/not-found? "test/httpserver/public/form"))) 
  (testing "Return 200 when GET on /form without data"
    (is (= [200]
           (handle-form "GET" 
                        "test/httpserver/public/form"
                        ""))))
  (testing "Return 200 and data when GET on /form with data"
    (handle-form "PUT" 
                 "test/httpserver/public/form"
                 "data=fatcat")
    (is (= [200 {} (response/content 
                     "test/httpserver/public/form")]
           (handle-form "GET" 
                        "test/httpserver/public/form"
                        "")))))

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
           (code/decode-base64 (restricted "GET" 
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
           (check-routes "GET" "/tea" "" {} "" "."))))
  (testing "Dynamic route for parameters"
    (is (= [200 {} "my = data\r\nyour = data"]
           (check-routes "GET" 
                         "/parameters" 
                         {"my" "data"
                          "your" "data"}
                         {}
                         ""
                         "."))))
  (testing "Use handle-form function when URI is /form"
    (is (= [200 {} "data=fatcat"]
           (check-routes "PUT"
                         "/form"
                         ""
                         {}
                         "data=fatcat"
                         "test/httpserver/public/form"))))
  (testing "Not in defined route"
    (is (nil? (check-routes "GET" 
                            "/not_a_route"
                            ""
                            {}
                            ""
                            ".")))))

(ns httpserver.logging-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [httpserver.logging :refer :all]))

(def get-request (str "GET / HTTP/1.1\r\n\r\n\r\n")) 

(def test-log-path "./test/httpserver/public")

(deftest test-log-request
  (testing "Log request with no headers or body"
    (log-request get-request test-log-path)
    (is (string/includes? 
          (slurp (str test-log-path "/logs"))
          get-request))))
                         
(deftest test-clear-log
  (testing "Clear log file"
    (clear-log test-log-path)
    (is (= ""
           (slurp (str test-log-path "/logs"))))))

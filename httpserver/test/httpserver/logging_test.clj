(ns httpserver.logging-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [httpserver.http-messages :refer :all]
            [httpserver.logging :refer :all]))

(deftest test-log-request
  (testing "Log request with no headers or body"
    (log-request dir-get-request test-path)
    (is (string/includes? 
          (slurp (str test-path "/logs"))
          dir-get-request))))

(deftest test-clear-log
  (testing "Clear log file"
    (clear-log test-path)
    (is (= ""
           (slurp (str test-path "/logs"))))))

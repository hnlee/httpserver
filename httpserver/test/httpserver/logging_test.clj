(ns httpserver.logging-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [httpserver.http-messages :refer :all]
            [httpserver.logging :refer :all]))

; There is a bug in these tests. Imagine if I deleted test-clear-log. Then test-log-request would stil pass,
; even without the code, because of leftover data in the file from previous runs. Ditto the test-clear-log.
; Each test should be independent, so the clear-should setup a non-empty log, and the log should setup
; an empty or missing file.
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

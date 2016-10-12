(ns httpserver.logging-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [httpserver.http-messages :as http]
            [httpserver.logging :refer :all]))

(def log-path (str http/test-path "/logs"))

(defn delete-log-file []
  (if (.exists (io/as-file log-path)) 
    (io/delete-file log-path)))

(deftest test-log-request
  (testing "Log request with no headers or body"
    (delete-log-file)
    (with-redefs [current-time
                  (fn [] "yyyy-mm-ddThh:mm:ss")]
      (log-request http/dir-get-request http/test-path)
      (is (= (slurp log-path)
             (str "yyyy-mm-ddThh:mm:ss\r\n"
                  http/dir-get-request
                  "\r\n-----\r\n\r\n"))))
    (delete-log-file)))

(deftest test-clear-log
  (testing "Clear log file"
    (spit log-path "a lot of logs")
    (clear-log http/test-path)
    (is (= "" (slurp log-path)))
    (delete-log-file)))

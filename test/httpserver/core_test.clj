(ns httpserver.core-test
  (:require [clojure.test :refer :all]
            [httpserver.core :refer :all])) 

(deftest test-set-vars 
  (testing "Use default settings if no flags"
    (is (= (hash-map :port 5000 :dir default-dir)
           (set-vars '()))))
  (testing "Set port when only dir is given"
    (is (= (hash-map :port 8888 :dir default-dir)
           (set-vars '("-p" "8888")))))
  (testing "Set dir when only port is given"
    (is (= (hash-map :port 5000 :dir "~")
           (set-vars '("-d" "~")))))
  (testing "Use given settings if both flags"
    (is (= (hash-map :port 8888 :dir "~")
           (set-vars '("-p" "8888" "-d" "~")))))
)


(ns httpserver.file-test
  (:require [clojure.test :refer :all]
            [httpserver.file :refer :all]))

(deftest test-not-found? 
  (testing "File that exists"
    (is (not (not-found? "./project.clj"))))
  (testing "File that does not exist"
    (is (not-found? "./nonsense")))
  (testing "Directory that exists"
    (is (not (not-found? "./src")))))

(deftest test-directory?
  (testing "Path to a directory"
    (is (directory? "./src")))
  (testing "Path to a file"
    (is (not (directory? "./project.clj")))))


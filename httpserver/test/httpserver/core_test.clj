(ns httpserver.core-test
  (:import (java.net Socket))
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [httpserver.socket :as socket]
            [httpserver.response :as response]
            [httpserver.http-messages :refer :all]
            [httpserver.core :refer :all])) 

(deftest test-set-vars 
  (testing "Use default settings if no flags"
    (is (= (hash-map :port 5000 :dir default-dir)
           (set-vars '()))))
  (testing "Set port when only dir is given"
    (is (= (hash-map :port 8888 :dir default-dir)
           (set-vars (list "-p" "8888")))))
  (testing "Set dir when only port is given"
    (is (= (hash-map :port 5000 :dir test-path)
           (set-vars (list "-d" test-path)))))
  (testing "Use given settings if both flags"
    (is (= (hash-map :port 8888 :dir test-path)
           (set-vars (list "-p" "8888" "-d" test-path)))))
)

(defn mock-router-fn [client-msg dir]
  (if (= client-msg 
         dir-get-request) (response/compose 409)
    nil)) 

(deftest test-route
  (testing "Default router behavior"
    (is (= (response/compose 404)
           (route not-found-get-request test-path))))
  (testing "Optional router behavior"
    (is (= (response/compose 409)
           (route dir-get-request 
                  test-path
                  mock-router-fn)))
    (is (= (response/compose 404)
           (route not-found-get-request 
                  test-path
                  mock-router-fn)))))

(deftest test-serve
  (with-open [server (socket/open 5000)
              client-socket (Socket. "localhost" 5000)
              client-out (io/writer client-socket)
              client-in (io/reader client-socket)
              connection (socket/listen server)]
    (testing "Server sends response to request"
      (.write client-out not-found-get-request)
      (.flush client-out)
      (serve connection ".")
      (is (= (string/trim-newline simple-404-response)
             (.readLine client-in))))
))

(deftest test-threading
  (with-open [server (socket/open 5000)
              client-one (Socket. "localhost" 5000)]
    (testing "Can accept second connection if one client already connected"
      (future (threading server "."))
      (.isConnected client-one)
      (with-open [client-two (Socket. "localhost" 5000)]
        (is (.isConnected client-two))))))

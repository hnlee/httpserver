(ns httpserver.core-test
  (:import (java.net Socket))
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [httpserver.socket :as socket]
            [httpserver.response :as response]
            [httpserver.http-messages :refer :all]
            [httpserver.core :refer :all])) 

(defn mock-router-fn [client-msg dir]
  (if (= client-msg 
         dir-get-request) (response/compose 409)
    nil))

(deftest test-set-vars 
  (testing "Use default settings if no flags"
    (is (= {:port default-port :dir default-dir :router nil}
           (set-vars '()))))
  (testing "Set dir and router when only port is given"
    (is (= {:port 8888 :dir default-dir :router nil}
           (set-vars (list "-p" "8888")))))
  (testing "Set port and router when only dir is given"
    (is (= {:port default-port :dir test-path :router nil}
           (set-vars (list "-d" test-path)))))
  (testing "Set port and dir when only router is given"
    (is (= {:port default-port
            :dir default-dir
            :router mock-router-fn}
           (set-vars (list "-r" mock-router-fn)))))
  (testing "Use given settings if all flags provided"
    (is (= {:port 8888 
            :dir test-path 
            :router mock-router-fn}
           (set-vars (list "-p" "8888" 
                           "-d" test-path
                           "-r" mock-router-fn))))))

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
  (testing "Server sends typical HTTP response to request"
    (with-open [server (socket/open 5000)
                client-socket (Socket. "localhost" 5000)
                client-out (io/writer client-socket)
                client-in (io/reader client-socket)
                connection (socket/listen server)]
      (.write client-out not-found-get-request)
      (.flush client-out)
      (serve connection test-path nil)
      (is (= (string/trim-newline simple-404-response)
             (.readLine client-in)))))
  (testing "Server sends custom HTTP response to request" 
   (with-open [server (socket/open 5000)
                client-socket (Socket. "localhost" 5000)
                client-out (io/writer client-socket)
                client-in (io/reader client-socket)
                connection (socket/listen server)]
      (.write client-out dir-get-request)
      (.flush client-out)
      (serve connection test-path mock-router-fn)
      (is (= (string/trim-newline simple-409-response)
             (.readLine client-in))))))

(deftest test-threading
  (with-open [server (socket/open 5000)
              client-one (Socket. "localhost" 5000)]
    (testing "Can accept second connection if one client already connected"
      (future (threading server test-path nil))
      (is (.isConnected client-one))
      (with-open [client-two (Socket. "localhost" 5000)]
        (is (.isConnected client-two))))))


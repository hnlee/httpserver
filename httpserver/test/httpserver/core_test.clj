(ns httpserver.core-test
  (:import (java.net Socket))
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as string]
            [httpserver.socket :as socket]
            [httpserver.response :as response]
            [httpserver.http-messages :as http]
            [httpserver.core :refer :all]))

(defn mock-router-fn [client-msg dir]
  (if (= client-msg http/dir-get-request) 
    (response/compose 409)
    nil))

(deftest test-default-dir
  (testing "Value is equal to $PUBLIC_DIR"
    (with-redefs [getenv (fn [env-var]
                           (if (not= env-var "PUBLIC_DIR") 
                             nil
                             http/test-path))]
      (is (= http/test-path (getenv "PUBLIC_DIR")))
      (is (= http/test-path (default-dir)))))
  (testing "If $PUBLIC_DIR is not set, value is local dir"
    (with-redefs [getenv (fn [env-var]
                           (if (= env-var "PUBLIC_DIR") 
                             nil
                             http/test-path))]
      (is (= "." (default-dir))))))

(deftest test-get-vars ;; lots of repeating of vars here
  (testing "Use default settings if no flags"
    (is (= {:port default-port
            :dir (default-dir)
            :router nil}
           (get-vars '()))))
  (testing "Set dir and router when only port is given"
    (is (= {:port 8888
            :dir (default-dir)
            :router nil}
           (get-vars (list "-p" "8888")))))
  (testing "Set port and router when only dir is given"
    (is (= {:port default-port
            :dir http/test-path
            :router nil}
           (get-vars (list "-d" http/test-path)))))
  (testing "Set port and dir when only router is given"
    (is (= {:port default-port
            :dir (default-dir)
            :router mock-router-fn}
           (get-vars (list "-r" mock-router-fn)))))
  (testing "Use given settings if all flags provided"
    (is (= {:port 8888
            :dir http/test-path
            :router mock-router-fn}
           (get-vars (list "-p" "8888"
                           "-d" http/test-path
                           "-r" mock-router-fn))))))

(deftest test-route
  (testing "Default router behavior"
    (is (= (response/compose 404)
           (route http/not-found-get-request
                  http/test-path))))
  (testing "Optional router behavior"
    (is (= (response/compose 409)
           (route http/dir-get-request
                  http/test-path
                  mock-router-fn)))
    (is (= (response/compose 404)
           (route http/not-found-get-request
                  http/test-path
                  mock-router-fn)))))

(deftest test-serve ;; the with-open is repeating the same stuff
  (testing "Server sends typical HTTP response to request"
    (with-open [server (socket/open 5000)
                client-socket (Socket. "localhost" 5000)
                client-out (io/writer client-socket)
                client-in (io/reader client-socket)
                connection (socket/listen server)]
      (.write client-out http/not-found-get-request)
      (.flush client-out)
      (serve connection http/test-path nil)
      (is (= (string/trim-newline http/simple-404-response)
             (.readLine client-in)))))
  (testing "Server sends custom HTTP response to request"
   (with-open [server (socket/open 5000)
                client-socket (Socket. "localhost" 5000)
                client-out (io/writer client-socket)
                client-in (io/reader client-socket)
                connection (socket/listen server)]
      (.write client-out http/dir-get-request)
      (.flush client-out)
      (serve connection http/test-path mock-router-fn)
      (is (= (string/trim-newline http/simple-409-response)
             (.readLine client-in))))))

(deftest test-threading
  (with-open [server (socket/open 5000)
              client-one (Socket. "localhost" 5000)]
    (testing "Can accept second connection if one client already connected"
      (future (threading server http/test-path nil))
      (is (.isConnected client-one))
      (with-open [client-two (Socket. "localhost" 5000)]
        (is (.isConnected client-two))))))


(ns cob-spec-app.core-test
  (:import [java.net Socket])
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [httpserver.socket :as socket] 
            [cob-spec-app.core :refer :all]))

(deftest test-run-server
  (with-open [server (socket/open 5000)
              client (Socket. "localhost" 5000)
              client-out (io/writer client)
              client-in (io/reader client)]
    (future (run-server server "test/cob_spec_app/public"))
    (testing "Connect to server"
      (is (.isConnected client)))
    (testing "Send to and receive from server"
      (.write client-out 
              "GET /not-a-route HTTP/1.1\r\n\r\n\r\n")
      (.flush client-out)
      (is (= "HTTP/1.1 404 Not found"
             (.readLine client-in))))
    (testing "Server returns cob_spec response"
      (.write client-out
              "GET /tea HTTP/1.1\r\n\r\n\r\n")
      (.flush client-out)
      (is (= "HTTP/1.1 200 OK")))))
 

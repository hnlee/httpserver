(ns httpserver.socket-test
  (:import (java.net ServerSocket Socket)
           (java.io StringReader))
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [httpserver.socket :refer :all]))

(deftest test-open
  (let [server-socket (open 5000)]
    (testing "Create a ServerSocket"
      (is (instance? ServerSocket server-socket)))
    (testing "Socket listening on given port"
      (is (= 5000 (.getLocalPort server-socket))))
    (.close server-socket)))

(deftest test-close
  (let [server-socket (open 5000)]
    (testing "Close socket"
      (close server-socket)
      (is (.isClosed server-socket))))
)

(deftest test-listen
  (with-open [server-socket (open 5000)
              client-socket (Socket. "localhost" 5000)]
    (testing "Client can connect to server"
      (is (.isConnected client-socket)))))

(def single-line-request (str "GET / HTTP/1.1\r\n"
                              "\r\n"))

(def multi-line-request (str "PUT /form HTTP/1.1\r\n"
                             "Content-Length: 9\r\n"
                             "\r\n"
                             "My=Data\r\n"))

(deftest test-body?
  (testing "Request with no Content-Length header"
    (is ((complement body?) single-line-request)))
  (testing "Request with Content-Length header" 
    (is (body? multi-line-request))))

(deftest test-read-head
  (testing "Request with no head"
    (with-open [input (StringReader. single-line-request)
                stream (io/reader input)]
      (is (= "GET / HTTP/1.1\r\n" 
             (read-head stream)))))
  (testing "Request with head"
    (with-open [input (StringReader. multi-line-request)
                stream (io/reader input)]
      (is (= (str "PUT /form HTTP/1.1\r\n"
                  "Content-Length: 9\r\n") 
             (read-head stream))))))

(deftest test-read-body
  (testing "Read message body from request"
    (with-open [input (StringReader. multi-line-request)
                stream (io/reader input)]
      (is (= "My=Data\r\n" 
             (read-body (read-head stream) stream))))))

(deftest test-receive
  (with-open [server-socket (open 5000)
              client-socket (Socket. "localhost" 5000)
              connection (listen server-socket)
              stream (io/writer client-socket)]
    (testing "Server can get input" 
      (.write stream single-line-request)
      (.flush stream) 
      (is (= single-line-request 
             (receive connection))))
    (comment
    (testing "Server can get multiline input"
      (let [request-msg ]
        (.write stream request-msg)
        (.flush stream)
        (is (= ((str/split-lines request-msg) 0)  
               ((receive connection) :request-line)))))
    )
))

(deftest test-give
  (with-open [server-socket (open 5000)
              client-socket (Socket. "localhost" 5000)
              connection (listen server-socket)
              stream (io/reader client-socket)]
    (testing "Server can send output" 
      (let [status-line "HTTP/1.1 200 OK\r\n"]
        (give connection status-line)
        (is (= (str/trim-newline status-line) 
               (.readLine stream)))))
))
 

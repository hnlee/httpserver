(ns httpserver.socket-test
  (:import (java.net ServerSocket Socket)
           (java.io StringReader))
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [httpserver.http-messages :as http]
            [httpserver.socket :refer :all]))

(deftest test-open
  (let [server-socket (open 5000)]
    (testing "Create a ServerSocket"
      (is (instance? ServerSocket server-socket)))
    (testing "Socket bound to given port"
      (is (= 5000 (.getLocalPort server-socket))))
    (.close server-socket)))

(deftest test-close
  (let [server-socket (open 5000)]
    (testing "Close socket"
      (close server-socket)
      (is (.isClosed server-socket)))))

(deftest test-listen
  (with-open [server-socket (open 5000)
              client-socket (Socket. "localhost" 5000)]
    (testing "Client can connect to server"
      (is (.isConnected client-socket)))))

(def request-line-only-request (str "GET / HTTP/1.1"
                                    crlf
                                    crlf
                                    crlf))

(def single-header-request (str "GET / HTTP/1.1"
                                crlf
                                "Host: localhost"
                                crlf
                                crlf))

(def multi-headers-request (str "GET / HTTP/1.1"
                                crlf
                                "Host: localhost"
                                crlf
                                "Accept: text/plain"
                                crlf
                                crlf))

(def zero-length-request (str "PUT /form HTTP/1.1"
                              crlf
                              "Content-Length: 0"
                              crlf
                              crlf))

(def headers-and-body-request (str "PUT /form HTTP/1.1"
                               crlf
                               "Content-Length: 7"
                               crlf
                               "Content-Type: text/plain"
                               crlf
                               crlf
                               "My=Data"))

(deftest test-body?
  (testing "Request with no headers"
    (is ((complement body?) request-line-only-request)))
  (testing "Request with no Content-Length header"
    (is ((complement body?) single-header-request)))
  (testing "Request with Content-Length header and body"
    (is (body? headers-and-body-request))))

(deftest test-read-request-line
  (testing "Get request line from request with no headers"
    (with-open [input (StringReader.
                        request-line-only-request)
                stream (io/reader input)]
      (is (= "GET / HTTP/1.1\r\n"
             (read-request-line stream)))))
  (testing "Get request line from request with headers"
    (with-open [input (StringReader. multi-headers-request)
                stream (io/reader input)]
      (is (= "GET / HTTP/1.1\r\n"
             (read-request-line stream))))))

(deftest test-read-headers
  (testing "Request with no headers"
    (with-open [input (StringReader.
                        request-line-only-request)
                stream (io/reader input)]
      (read-request-line stream)
      (is (= crlf 
             (read-headers stream)))))
  (testing "Request with one headers"
    (with-open [input (StringReader. single-header-request)
                stream (io/reader input)]
      (read-request-line stream)
      (is (= (str "Host: localhost" crlf)
             (read-headers stream)))))
   (testing "Request with multiple headers"
    (with-open [input (StringReader. multi-headers-request)
                stream (io/reader input)]
      (read-request-line stream)
      (is (= (str "Host: localhost"
                  crlf
                  "Accept: text/plain"
                  crlf)
             (read-headers stream)))))
   (testing "Request with headers and body"
     (with-open [input (StringReader.
                         headers-and-body-request)
                 stream (io/reader input)]
       (read-request-line stream)
       (is (= (str "Content-Length: 7"
                   crlf
                   "Content-Type: text/plain"
                   crlf)
              (read-headers stream))))))

(deftest test-read-body
  (testing "Read request with zero-length body"
    (with-open [input (StringReader.
                        zero-length-request)
                stream (io/reader input)]
      (is (= "" 
             (read-body (read-headers stream) stream)))))
  (testing "Read request with headers and body"
    (with-open [input (StringReader.
                        headers-and-body-request)
                stream (io/reader input)]
      (is (= "My=Data"
             (read-body (read-headers stream) stream))))))

(deftest test-receive
  (with-open [server-socket (open 5000)
              client-socket (Socket. "localhost" 5000)
              connection (listen server-socket)
              stream (io/writer client-socket)]
    (testing "Server can get input"
      (.write stream request-line-only-request)
      (.flush stream)
      (is (= request-line-only-request 
             (receive connection))))
    (testing "Server can get request with headers"
      (.write stream multi-headers-request)
      (.flush stream)
      (is (= multi-headers-request 
             (receive connection))))
    (testing "Server can get request with headers and body"
      (.write stream headers-and-body-request)
      (.flush stream)
      (is (= headers-and-body-request 
             (receive connection))))))

(deftest test-give
  (with-open [server-socket (open 5000)
              client-socket (Socket. "localhost" 5000)
              connection (listen server-socket)
              stream (io/reader client-socket)]
    (testing "Server can send output"
      (let [status-line "HTTP/1.1 200 OK\r\n"
            output (map (comp byte int) status-line)]
        (give connection output)
        (is (= (str/trim-newline status-line)
               (.readLine stream)))))))


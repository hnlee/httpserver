(ns httpserver.http-messages
  (:require [httpserver.encoding :as code]))

(def test-path "test/httpserver/public")

(def request-string (str "%s %s HTTP/1.1\r\n"
                         "%s\r\n"
                         "\r\n"
                         "%s"))

(def response-string (str "HTTP/1.1 %d %s\r\n"))

(def simple-200-response (format response-string
                                 200
                                 "OK"))

(def simple-404-response (format response-string
                          404
                          "Not found"))

(def tea-get-request (format request-string
                             "GET" "/tea" "" ""))

(def parameters-get-request 
  (format request-string
          "GET"
          "/parameters?my=data&your=data"
          ""
          ""))

(def form-put-request (format request-string
                              "PUT"
                              "/form"
                              ""
                              "data=fatcat"))

(def not-found-get-request (format request-string
                                   "GET"
                                   "/not_a_route"
                                   ""
                                   ""))

(def dir-get-request (format request-string
                             "GET" "/" "" ""))

(def valid-head-request (format request-string
                                "HEAD" "/" "" ""))

(def text-get-request (format request-string
                              "GET" "/file1" "" ""))

(def encoded-get-request 
  (format request-string
          "GET" "/%66%69%6C%65%31" "" ""))

(def bogus-request (format request-string
                           "BOGUS" "/" "" ""))

(def restricted-request-no-credentials
  (format request-string
          "GET" "/logs" "" ""))

(def restricted-request-with-credentials
  (format request-string
          "GET" 
          "/logs"
          (str "Authorization: Basic "
               (code/encode-base64 "admin:hunter2"))
          ""))

(def partial-get-request 
  (format request-string
          "GET"
          "/partial_content.txt"
          "Range: bytes=0-4" 
          ""))

(def valid-patch-request 
  (format request-string
          "PATCH"
          "/patch-content.txt"
          (str "If-Match: "
               (code/encode-sha1 "default content")) 
          "patched content"))


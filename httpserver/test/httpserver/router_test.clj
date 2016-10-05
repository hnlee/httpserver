(ns httpserver.router-test
  (:require [clojure.test :refer :all]
            [httpserver.router :refer :all]
            [httpserver.encoding :as code]
            [httpserver.routes :as routes]
            [httpserver.request :as request]
            [httpserver.response :as response]))

(def request-string (str "%s %s HTTP/1.1\r\n"
                         "%s\r\n"
                         "\r\n"
                         "%s"))

(def response-string (str "HTTP/1.1 %d %s\r\n"))

(def dir-get-request (format request-string
                             "GET" "/" "" ""))

(def invalid-head-request (format request-string
                                  "HEAD" "/foobar" "" ""))

(def valid-head-request (format request-string
                                "HEAD" "/" "" ""))

(def coffee-get-request (format request-string
                                "GET"
                                "/coffee"
                                "" ""))

(def text-get-request (format request-string
                              "GET"
                              "/file1"
                              "" ""))

(def encoded-tea-request (format request-string
                                 "GET"
                                 "/%74%65%61"
                                 "" ""))

(def query-request (format request-string
                           "GET"
                           "/parameters?my=data&your=data"
                           "" ""))

(def bogus-request (format request-string
                           "BOGUS"
                           "/"
                           "" ""))

(def restricted-request-without-credentials
  (format request-string
          "GET"
          "/logs"
          "" ""))

(def restricted-request-with-credentials
  (format request-string
          "GET"
          "/logs"
          (str "Authorization: Basic "
               (code/encode-base64 "admin:hunter2"))
          ""))

(def partial-get-request (format request-string
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

(deftest test-parse-parameters
  (testing "Query with no parameters"
    (is (= "data"
           (parse-parameters "data"))))
  (testing "Query with single parameter"
    (is (= {"my" "data"}
           (parse-parameters "my=data"))))
  (testing "Query with multiple parameters"
    (is (= {"my" "data"
            "your" "data"}
           (parse-parameters "my=data&your=data")))))

(deftest test-parse-query
  (testing "URI without query"
    (is (= {:uri "/form"
            :query ""}
           (parse-query "/form"))))
  (testing "URI with query"
    (is (= {:uri "/form" 
            :query (parse-parameters "my=data")} 
           (parse-query "/form?my=data"))))
  (testing "URI with multiple variables in query"
    (is (= {:uri "/form"
            :query (parse-parameters "my=data&your=data")}
           (parse-query "/form?my=data&your=data")))))
 
(deftest test-standard-get
  (testing "Return 200 with requested URI content in msg body"
    (is (= (response/compose 200
                             {}
                             (response/content "./"))
           (standard-get "./")))))

(deftest test-credentials?
  (testing "No credentials in header"
    (is (not (credentials? {} "username:password"))))
  (testing "Incorrect credentials in header"
    (is (not (credentials? {"Authorization"
                            "Basic random-string"}
                           "username:password"))))
  (testing "Correct credentials in header"
    (is (credentials? {"Authorization"
                       "Basic username:password"}
                      "username:password"))))

(deftest test-authorize
  (testing "Return 401 if no credentials in header"
    (is (= (response/compose 401 
                             {"WWW-Authenticate"
                              "Basic realm=\"Admin\""})
           (authorize {} 
                      "username:password"
                      "/"))))
  (testing "Return 401 if incorrect credentials"
    (is (= (response/compose 401
                             {"WWW-Authenticate"
                              "Basic realm=\"Admin\""})
           (authorize {"Authorization"
                       "Basic random-string"}
                      "username:password"
                      "/")))) 
  (testing "Return 200 if correct credentials"
    (is (= (standard-get "/") 
           (authorize {"Authorization"
                       "Basic username:password"}
                      "username:password"
                      "/")))))

(deftest test-range?
  (testing "Request without range header"
    (is (not (range? {"Content-Length" "7"}))))
  (testing "Request with range header"
    (is (range? {"Range" "bytes=0-4"}))))

(deftest test-parse-range
  (let [path "test/httpserver/public/partial_content.txt"]
    (testing "Range with both start and end indices"
      (is (= (response/content path 0 4)
             (parse-range {"Range" "bytes=0-4"}
                          path))))
    (testing "Range with just end index"
      (is (= (response/content path nil 6)
             (parse-range {"Range" "bytes=-6"}
                          path))))
    (testing "Range with just start index"
      (is (= (response/content path 4 nil)
             (parse-range {"Range" "bytes=4-"}
                          path))))))

(deftest test-etag?
  (let [path "test/httpserver/public/patch-content.txt"]
    (testing "No If-Match header"
      (is (not (etag? {"Content-Length" "7"}
                      path))))
    (testing "Header but wrong etag"
      (is (not (etag? {"If-Match" "not-a-tag"}
                      path))))
    (testing "Header with right etag"
      (is (etag? {"If-Match"
                  "dc50a0d27dda2eee9f65644cd7e4c9cf11de8bec"}
                 path)))))

(deftest test-standard-patch
  (let [path "test/httpserver/public/patch-content.txt"]
    (testing "Return 204 response with etag"
      (is (= (response/compose 
               204
               {"ETag" (code/encode-sha1 "patched content")})
             (standard-patch {"If-Match"
                              (code/encode-sha1 "default content")}
                             "patched content"
                             path)))
      (standard-patch {"If-Match"
                       (code/encode-sha1 "patched content")}
                      "default content" 
                      path))
    (testing "Update requested path with body"
      (standard-patch {"If-Match"
                       (code/encode-sha1 "default content")}
                      "patched content"
                       path)
      (is (= "patched content"
             (slurp path)))
      (standard-patch {"If-Match"
                       (code/encode-sha1 "patched content")}
                      "default content" 
                      path))
    (testing "Invalid etag in patch request")
      (is (= (response/compose 409) 
             (standard-patch {"If-Match" "nonsense"}
                             "patched content"
                             path)))))

(deftest test-choose-response
  (testing "Invalid URI returns 404 response"
    (is (= (response/compose 404) 
           (choose-response invalid-head-request ".")))) 
  (testing "Able to get hard-coded route from hashmap"
    (is (= (response/compose 418
                             {}
                             "I'm a teapot")
           (choose-response coffee-get-request ".")))) 
  (testing "HEAD on valid URI returns 200 response with no body" 
    (is (= (response/compose 200)
            (choose-response valid-head-request "."))))
  (testing "GET on text file returns content in body"
    (is (= (standard-get "test/httpserver/public/file1")
           (choose-response text-get-request
                            "test/httpserver/public"))))
  (testing "URI with encoded characters is decoded"
    (is (= (response/compose 200)
           (choose-response encoded-tea-request ".")))) 
  (testing "URI with query string is parsed"
    (is (= (response/compose 200
                             {}
                             "my = data\r\nyour = data")
           (choose-response query-request ".")))) 
  (testing "Return 405 to bogus request"
    (is (= (response/compose 405)
           (choose-response bogus-request "."))))
  (testing "Return 401 to /logs without credentials"
    (is (= (response/compose 401
                             {"WWW-Authenticate"
                              "Basic realm=\"Admin\""})
           (choose-response 
             restricted-request-without-credentials 
             "."))))
  (testing "Return 200 and content to /logs with credentials"
    (is (= (standard-get "test/httpserver/public/logs")
           (choose-response
             restricted-request-with-credentials
             "test/httpserver/public"))))
  (testing "Return 206 and partial content"
    (is (= (response/compose 206
                             {}
                             (response/content "test/httpserver/public/partial_content.txt" 0 4))
           (choose-response partial-get-request
                            "test/httpserver/public"))))
  (testing "Return 204 to valid PATCH request"
    (is (= (response/compose 204
                             {"ETag"
                              (code/encode-sha1 "patched content")})
           (choose-response valid-patch-request
                            "test/httpserver/public")))
    (standard-patch {"If-Match"
                     (code/encode-sha1 "patched content")} 
                    "default content"
                    "test/httpserver/public/patch-content.txt")))

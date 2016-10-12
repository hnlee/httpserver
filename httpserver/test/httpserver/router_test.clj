(ns httpserver.router-test
  (:require [clojure.test :refer :all]
            [httpserver.http-messages :refer :all]
            [httpserver.router :refer :all]
            [httpserver.encoding :as code]
            [httpserver.request :as request]
            [httpserver.response :as response]))

(deftest test-standard-get
  (testing "Return 200 with requested URI content in msg body"
    (let [dir-path (str test-path "/")]
      (is (= (response/compose 200
                               {}
                               (response/content dir-path))
             (standard-get dir-path))))))

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
  (let [auth-header {"WWW-Authenticate"
                     "Basic realm=\"Admin\""}]
    (testing "Return 401 if no credentials in header"
      (is (= (response/compose 401
                               auth-header)
             (authorize {}
                        "username:password"
                        "/"))))
    (testing "Return 401 if incorrect credentials"
      (is (= (response/compose 401
                               auth-header)
             (authorize {"Authorization"
                         "Basic random-string"}
                        "username:password"
                        test-path))))
    (testing "Return 200 if correct credentials"
      (is (= (standard-get test-path)
             (authorize {"Authorization"
                         "Basic username:password"}
                        "username:password"
                        test-path))))))

(deftest test-range?
  (testing "Request without range header"
    (is (not (range? {"Content-Length" "7"}))))
  (testing "Request with range header"
    (is (range? {"Range" "bytes=0-4"}))))

(deftest test-parse-indices
  (testing "Range has both start and end indices"
    (is (= [0 4] (parse-indices "0-4"))))
  (testing "Range has just end index"
    (is (= [nil 6] (parse-indices "-6"))))
  (testing "Range has just start index"
    (is (= [4 nil] (parse-indices "4-")))))

(deftest test-parse-range
  (let [partial-path (str test-path "/partial_content.txt")]
    (testing "Range with both start and end indices"
      (is (= (response/content partial-path 0 4)
             (parse-range {"Range" "bytes=0-4"}
                          partial-path))))
    (testing "Range with just end index"
      (is (= (response/content partial-path nil 6)
             (parse-range {"Range" "bytes=-6"}
                          partial-path))))
    (testing "Range with just start index"
      (is (= (response/content partial-path 4 nil)
             (parse-range {"Range" "bytes=4-"}
                          partial-path))))))

(deftest test-etag?
  (let [patch-path (str test-path "/patch-content.txt")]
    (testing "No If-Match header"
      (is (not (etag? {"Content-Length" "7"}
                      patch-path))))
    (testing "Header but wrong etag"
      (is (not (etag? {"If-Match" "not-a-tag"}
                      patch-path))))
    (testing "Header with right etag"
      (is (etag? {"If-Match"
                  (code/encode-sha1 "default content")}
                 patch-path)))))

(deftest test-standard-patch
  (let [patch-path (str test-path "/patch-content.txt")]
    (testing "Return 204 response with etag"
      (is (= (response/compose
               204
               {"ETag"
                (code/encode-sha1 "patched content")})
             (standard-patch
               {"If-Match"
                (code/encode-sha1 "default content")}
               "patched content"
               patch-path)))
      (standard-patch {"If-Match"
                       (code/encode-sha1 "patched content")}
                      "default content"
                      patch-path))
    (testing "Update requested path with body"
      (standard-patch {"If-Match"
                       (code/encode-sha1 "default content")}
                      "patched content"
                       patch-path)
      (is (= "patched content"
             (slurp patch-path)))
      (standard-patch {"If-Match"
                       (code/encode-sha1 "patched content")}
                      "default content"
                      patch-path))
    (testing "Invalid etag in patch request")
      (is (= (response/compose 409)
             (standard-patch {"If-Match" "nonsense"}
                             "patched content"
                             patch-path)))))

(deftest test-choose-response
  (testing "Invalid URI returns 404 response"
    (is (= (response/compose 404)
           (choose-response not-found-get-request
                            test-path))))
  (testing "HEAD on valid URI returns 200 response with no body"
    (is (= (response/compose 200)
           (choose-response valid-head-request
                            test-path))))
  (testing "GET on text file returns content in body"
    (is (= (standard-get (str test-path "/file1"))
           (choose-response text-get-request
                            test-path))))
  (testing "URI with encoded characters is decoded"
    (is (= (standard-get (str test-path "/file1"))
           (choose-response encoded-get-request
                            test-path))))
  (testing "Return 405 to bogus request"
    (is (= (response/compose 405)
           (choose-response bogus-request
                            test-path))))
  (testing "Return 206 and partial content"
    (is (= (response/compose
             206
             {}
             (response/content (str test-path
                                    "/partial_content.txt")
                               0 4))
           (choose-response partial-get-request
                            test-path))))
  (testing "Return 204 to valid PATCH request"
    (is (= (response/compose
             204
             {"ETag" (code/encode-sha1 "patched content")})
           (choose-response valid-patch-request
                            test-path)))
    (standard-patch {"If-Match"
                     (code/encode-sha1 "patched content")}
                    "default content"
                    (str test-path "/patch-content.txt"))))

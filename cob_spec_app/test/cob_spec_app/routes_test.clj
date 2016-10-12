(ns cob-spec-app.routes-test
  (:require [clojure.test :refer :all]
            [httpserver.encoding :as code]
            [httpserver.file :as file]
            [httpserver.response :as response]
            [httpserver.http-messages :refer :all]
            [httpserver.routes :refer :all]))

(def path "test/cob_spec_app/public")

(deftest test-handle-form
  (let [data-fatcat "data=fatcat"
        data-heathcliff "data=heathcliff"
        form-path (str path "/form")]
    (testing "Return 200 and update /form content when POST"
      (is (= (response/compose 200
                               {}
                               data-fatcat)
             (handle-form "POST"
                          form-path
                          data-fatcat)))
      (is (= data-fatcat
             (slurp form-path))))
   (testing "Return 200 and update /form content when PUT"
      (is (= (response/compose 200
                               {}
                               data-heathcliff)
             (handle-form "PUT"
                          form-path
                          data-heathcliff)))
      (is (= data-heathcliff
             (slurp form-path))))
    (testing "Return 200 and delete /form content when DELETE"
      (is (= (code/str->bytes simple-200-response)
             (handle-form "DELETE"
                          form-path
                          "")))
      (is (file/not-found? form-path)))
    (testing "Return 200 when GET on /form without data"
      (is (= (code/str->bytes simple-200-response)
             (handle-form "GET"
                          form-path
                          ""))))
    (testing "Return 200 and data when GET on /form with data"
      (handle-form "PUT"
                   form-path
                   "data=fatcat")
      (is (= (response/compose
               200
               {}
               (response/content form-path))
             (handle-form "GET"
                          form-path
                          ""))))))

(deftest test-parameters?
  (testing "Return true if GET for /parameters"
    (is (parameters? "GET" "/parameters")))
  (testing "Return false if not a GET request"
    (is (not (parameters? "PUT" "/parameters"))))
  (testing "Return false if not /parameters"
    (is (not (parameters? "GET" "/")))))

(deftest test-restricted
  (testing "Return credentials if GET for /logs"
    (is (= "admin:hunter2"
           (code/decode-base64 (restricted "GET"
                                           "/logs")))))
  (testing "Return nil if not a GET request"
    (is (nil? (restricted "PUT" "/logs"))))
  (testing "Return nil if not a restricted URI"
    (is (nil? (restricted "GET" "/")))))

(deftest test-format-query
  (testing "Format query with no parameters"
    (is (= "data"
           (format-query "data"))))
  (testing "Format query with single parameter"
    (is (= "my = data"
           (format-query {"my" "data"}))))
  (testing "Format multiple parameters in query"
    (is (= "my = data\r\nyour = data"
           (format-query {"my" "data"
                          "your" "data"})))))

(deftest test-route?
  (testing "Not a route"
    (is (not (route? "GET" "/notaroute"))))
  (testing "Is a route"
    (is (route? "GET" "/tea"))))

(deftest test-choose-response
  (testing "Hard-coded route"
    (is (= (code/str->bytes simple-200-response)
           (choose-response tea-get-request
                            path))))
  (testing "Dynamic route for parameters"
    (let [body "my = data\r\nyour = data"]
      (is (= (response/compose 200 {} body)
             (choose-response parameters-get-request
                              path)))))
  (testing "Use handle-form function when URI is /form"
    (let [body "data=fatcat"]
      (is (= (response/compose 200 {} body)
             (choose-response form-put-request
                              path)))))
  (testing "Not in defined route"
    (is (nil? (choose-response not-found-get-request
                               path)))))

(ns httpserver.router
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [httpserver.encoding :as code]
            [httpserver.file :as file]
            [httpserver.request :as request]
            [httpserver.response :as response]))

(def http-methods
  #{"OPTIONS"
    "GET"
    "HEAD"
    "POST"
    "PUT"
    "DELETE"
    "TRACE"
    "CONNECT"
    "PATCH"})

(defn not-allowed? [method]
  (not (contains? http-methods method)))

(defn standard-get [path]
  (response/compose 200
                    {}
                    (response/content path)))

(defn credentials? [headers credentials]
  (and (contains? headers "Authorization")
       (= (headers "Authorization")
          (str "Basic " credentials))))

(defn authorize [headers credentials path]
  (if (credentials? headers credentials) 
    (standard-get path)
    (response/compose 401
                      {"WWW-Authenticate"
                       "Basic realm=\"Admin\""})))

(defn range? [headers]
  (contains? headers "Range"))

(defn parse-indices [indices]
  (let [[start end] (string/split indices #"-")]
    [(if (= "" start) 
       nil
       (Integer. start))
     (if (nil? end) 
       end
       (Integer. end))]))

(defn parse-range [headers path]
  (let [value (last (string/split (headers "Range")
                                  #"="))
        [start end] (parse-indices value)]
    (response/content path start end)))

(defn etag? [headers path]
  (and (contains? headers "If-Match")
       (= (headers "If-Match")
          (code/encode-sha1 (response/content path)))))

(defn standard-patch [headers body path]
  (if (etag? headers path) 
    (do (spit path body)
        (response/compose 204
                          {"ETag" (code/encode-sha1 body)}))
    (response/compose 409)))

(defn choose-response [client-msg dir]
  (let [{method :method
         uri :uri
         query :query
         headers :headers
         body :body} (request/parse client-msg)
        path (str dir uri)]
    (cond
      (not-allowed? method) (response/compose 405)
      (file/not-found? path) (response/compose 404)
      (range? headers) (response/compose
                         206
                         {}
                         (parse-range headers path))
      (= method "PATCH") (standard-patch headers
                                         body
                                         path)
      (= method "HEAD") (response/compose 200)
      (= method "GET") (standard-get path))))


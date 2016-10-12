(ns httpserver.request
  (:require [clojure.string :as string]
            [httpserver.encoding :as code]))

(defn parse-parameters [parameters]
  (if (string/includes?
        parameters
        "=") (as-> parameters vars
                  (string/split vars #"&")
                  (map #(string/split % #"=") vars)
                  (reduce concat vars)
                  (map code/decode-uri vars)
                  (apply hash-map vars))
    (code/decode-uri parameters)))

(defn parse-query [uri]
  (if-let [[uri base-uri query]
           (re-find #"(.*)\?(.*)$" uri)]
    {:uri (code/decode-uri base-uri)
     :query (parse-parameters query)}
    {:uri (code/decode-uri uri)
     :query ""}))

(defn parse-request-line [request-line]
  (let [[all method uri version]
        (re-find #"^([A-Z]+) (.+) (HTTP.+)"
                 request-line)]
    (merge {:method method}
           (parse-query uri))))

(defn parse-headers [headers]
  (if-not
    (= "" headers) (as-> headers lines
                         (string/split lines #"\r\n")
                         (map #(string/split % #": ")
                              lines)
                         (apply concat lines)
                         (apply hash-map lines))
    {}))


(defn parse [msg]
  (let [[all request-line headers body]
        (re-find #"(?s)(.+?)\r\n(.*)\r\n\r\n(.*)" msg)]
    (merge (parse-request-line request-line)
           {:headers (parse-headers headers)
            :body body})))

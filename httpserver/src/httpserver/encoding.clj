(ns httpserver.encoding
  (:import (java.util Base64)
           (java.security MessageDigest))
  (:require [clojure.string :as string]))

(defn str->bytes [a-string]
  (map byte a-string))

(defn bytes->str [a-byte-array] 
  (string/join "" (map char a-byte-array)))

(defn bytes->hex [a-byte-seq]
  (->> a-byte-seq
       (map #(format "%02x" %))
       (string/join "")))

(defn decode-uri [uri]
  (string/replace uri 
                  #"(?i)%[0-9a-f]{2}"
                  (fn [encoded-string]
                    (-> encoded-string
                        (subs 1)
                        (Byte/parseByte 16)
                        (char)
                        (str)))))

(defn encode-uri [uri]
  (string/replace 
    uri 
    #"[^\w/]+"
    (fn [decoded-string]
      (->> decoded-string
           (map byte)
           (map #(format "%%%02X" %))
           (string/join "")))))

(defn decode-base64 [encoded-string]
  (let [decoder (Base64/getDecoder)]
    (->> encoded-string
         (.decode decoder)
         (bytes->str))))

(defn encode-base64 [decoded-string]
  (let [encoder (Base64/getEncoder)]
    (->> decoded-string
         (str->bytes)
         (byte-array)
         (.encodeToString encoder))))

(defn encode-sha1 [decoded-string]
  (let [encoder (MessageDigest/getInstance "SHA-1")] 
    (->> decoded-string
        (str->bytes) 
        (byte-array)
        (.digest encoder)
        (bytes->hex))))

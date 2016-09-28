(ns httpserver.encoding
  (:import (java.util Base64))
  (:require [clojure.string :as string]))

(defn decode-uri [uri]
  (string/replace uri 
                  #"(?i)%[0-9a-f]{2}"
                  #(str (char (Integer/parseInt (subs % 1) 
                                                16)))))

(defn encode-uri [uri]
  (string/replace 
    uri 
    #"[^\w/]+"
    (fn [unencoded]
      (->> unencoded
           (map (comp int char))
           (map #(Integer/toHexString %))
           (map string/upper-case)
           (map #(str "%" %)) 
           (string/join "")))))

(defn decode-base64 [encoded-string]
  (let [decoder (Base64/getDecoder)]
    (string/join "" 
                 (map char 
                      (.decode decoder encoded-string)))))

(defn encode-base64 [decoded-string]
  (let [encoder (Base64/getEncoder)]
    (.encodeToString encoder  
                     (byte-array (map (comp byte int)
                                      decoded-string)))))

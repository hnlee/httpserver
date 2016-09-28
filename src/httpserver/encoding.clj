(ns httpserver.encoding
  (:import (java.util Base64))
  (:require [clojure.string :as string]))

(defn decode-url [uri]
  (string/replace uri 
                  #"(?i)%[0-9a-f]{2}"
                  #(str (char (Integer/parseInt (subs % 1) 
                                                16)))))

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

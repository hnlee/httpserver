(ns httpserver.authentication
  (:import (java.util Base64))
  (:require [clojure.string :as string]))

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

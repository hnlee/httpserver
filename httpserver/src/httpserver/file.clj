(ns httpserver.file
  (:require [clojure.java.io :as io]))

(defn not-found? [path]
  (not (.exists (io/as-file path)))) 

(defn directory? [path]
  (.isDirectory (io/as-file path)))

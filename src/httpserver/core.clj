(ns httpserver.core
  (:gen-class)
  (:require [httpserver.operator :as operator]))

(defn -main
  "Runs application"
  [& args]
  (when-let [flags (apply hash-map args)]
    (operator/serve
      (if (contains? flags "-p") (Integer. (flags "-p"))
                                 5000)
      (if (contains? flags "-d") (flags "-d")
                                 "$PUBLIC_DIR")
    )))
      

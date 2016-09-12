(ns httpserver.core
  (:gen-class)
  (:require [httpserver.operator :as operator]
            [httpserver.socket :as socket]))

(defn set-vars [args]
  (if (= 0 (count args)) (hash-map :port 5000 
                                   :dir "$PUBLIC_DIR")
    (let [flags (apply hash-map args)]
      (hash-map :port (Integer. (get-in flags ["-p"] "5000"))
                :dir (get-in flags ["-d"] "$PUBLIC_DIR")))))

(defn -main [& args]
  (let [vars (set-vars args)]
    (with-open [server (socket/open (vars :port))
                connection (socket/listen server)]
      (operator/serve connection :dir))))  


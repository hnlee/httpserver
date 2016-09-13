(ns httpserver.core
  (:gen-class)
  (:require [httpserver.operator :as operator]
            [httpserver.socket :as socket]))

(def default-port 5000)

(def default-dir 
  (let [public (System/getenv "PUBLIC_DIR")]
    (if (nil? public) "." public)))

(defn set-vars [args]
  (let [flags (apply hash-map args)]
    (hash-map :port (Integer. (get-in flags 
                                      ["-p"]
                                      default-port))
              :dir (get-in flags 
                           ["-d"] 
                           default-dir))))

(defn -main [& args]
  (let [vars (set-vars args)]
    (with-open [server (socket/open (vars :port))
                connection (socket/listen server)]
      (operator/serve connection (vars :dir))))) 


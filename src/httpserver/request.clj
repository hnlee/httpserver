(ns httpserver.request)

(defn parse [request-msg]
  (let [regex (re-find #"^([A-Z]+).*" 
                       (request-msg :request-line))]
    (hash-map :method (regex 1))))

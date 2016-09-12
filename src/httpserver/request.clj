(ns httpserver.request)

(defn parse [request-msg]
  (let [regex (re-find #"^([A-Z]+) (.+) (HTTP.+)$" 
                       (request-msg :request-line))]
    (hash-map :method (regex 1)
              :uri (regex 2))))

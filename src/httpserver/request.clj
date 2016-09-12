(ns httpserver.request)

(defn parse [request-line]
  (let [regex (re-find #"^([A-Z]+).*\r\n" request-line)]
    (hash-map :method (regex 1))))

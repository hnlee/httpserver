(ns httpserver.request)

(defn parse [request-line]
  (hash-map :method "GET"))

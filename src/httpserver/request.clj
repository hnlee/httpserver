(ns httpserver.request)

(defn parse [msg]
  (let [regex (re-find #"^([A-Z]+) (.+) (HTTP.+)\r\n" msg)]
    {:method (regex 1)
     :uri (regex 2)}))

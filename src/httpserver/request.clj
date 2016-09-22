(ns httpserver.request)

(defn parse [msg]
  (let [[all method uri version] 
        (re-find #"^([A-Z]+) (.+) (HTTP.+)\r\n" msg)]
    {:method method
     :uri uri}))

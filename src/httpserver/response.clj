(ns httpserver.response)

(defn compose [status-code]
  "HTTP/1.1 200 OK\r\nConnection: close\r\n\r\n")

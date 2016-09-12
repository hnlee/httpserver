(ns httpserver.response)

(defn compose [status-code]
  (let [reason-phrase (hash-map 200 "OK"
                               404 "Not found")]
    (format "HTTP/1.1 %d %s\r\n"
           status-code
           (reason-phrase status-code))))

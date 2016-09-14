(ns httpserver.response)

(def status-line "%s %d %s\r\n") 

(def reason-phrase {200 "OK"
                    404 "Not found"})

(defn compose [status-code]
  (format status-line 
          "HTTP/1.1"
          status-code
          (reason-phrase status-code)))

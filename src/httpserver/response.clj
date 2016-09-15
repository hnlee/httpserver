(ns httpserver.response)

(def status-line (str "%s %d %s\r\n"))

(def reason-phrase {200 "OK"
                    404 "Not found"
                    418 "I'm a teapot"})

(defn compose 
  "Option to provide headers and message body in params"
  ([status-code] 
    (format status-line "HTTP/1.1"
                        status-code
                        (reason-phrase status-code)))
  ([status-code headers-map]
    (str (format status-line "HTTP/1.1"
                             status-code
                             (reason-phrase status-code))
         (apply str (for [header (keys headers-map)]
                      (str header 
                           ": " 
                           (headers-map header)
                           "\r\n")))
         "\r\n")) 
)

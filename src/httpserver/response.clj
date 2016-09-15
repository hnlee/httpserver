(ns httpserver.response)

(def status-line (str "%s %d %s\r\n"))

(def reason-phrase {200 "OK"
                    404 "Not found"
                    418 "I'm a teapot"})

(defn format-status-line [status-code]
  (format status-line
          "HTTP/1.1"
          status-code
          (reason-phrase status-code)))

(defn format-headers [headers-map]
  (apply str 
         (for [header (keys headers-map)]
           (str header ": " (headers-map header) "\r\n"))))

(defn compose 
  "Option to provide headers and message body in params"
  ([status-code] (format-status-line status-code)) 
  ([status-code headers-map]
    (str (format-status-line status-code) 
         (format-headers headers-map)
         "\r\n"))
  ([status-code headers-map body]
   (if (contains? 
         headers-map 
         "Content-Length") (str (format-status-line 
                                  status-code)
                                (format-headers 
                                  headers-map)
                                "\r\n"
                                body)
     (compose status-code
              (merge headers-map
                     {"Content-Length" (count body)})
              body)))
)

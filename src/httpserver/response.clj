(ns httpserver.response
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

(def status-line (str "%s %d %s\r\n"))

(def reason-phrase {200 "OK"
                    404 "Not found"
                    418 "I'm a teapot"
                    302 "Found"})

(defn format-status-line [status-code]
  (format status-line
          "HTTP/1.1"
          status-code
          (reason-phrase status-code)))

(defn format-headers [headers-map]
  (apply str 
         (for [header (keys headers-map)]
           (str header ": " (headers-map header) "\r\n"))))

(defn str->bytes [string]
  (map (comp byte int) string))

(defn linkify [paths]
  (string/join (map (fn [path] (str "<a href=\"/"
                                    path
                                    "\">"
                                    path
                                    "</a><br />")) paths))) 

(defn htmlify [title body]
  (str "<html><head><title>"
       title
       "</title></head><body>"
       body
       "</body></html>"))

(defn ls [path]
  (apply list (.list (io/as-file path))))

(defn directory? [path]
  (.isDirectory (io/as-file path)))

(defn content [path]
  (if (directory? path) (htmlify (str "Index of "
                                      ((re-find #".*(/.*?)$"
                                                path) 1))
                                 (linkify (ls path)))
    (let [file (io/as-file path)]
      (with-open [stream (io/input-stream file)] 
        (vec (repeatedly (.length file) 
                         #(.read stream)))))))

(defn content-type [path]
  (cond 
    (directory? path) "text/html"
    ((complement nil?) 
      (re-find #"(?i)\.jpe{0,1}g$" path)) "image/jpeg" 
    :else
    "text/plain"))

(defn compose 
  "Option to provide headers and message body in params"
  ([status-code] 
    (str->bytes (format-status-line status-code))) 
  ([status-code headers-map]
    (str->bytes (str (format-status-line status-code) 
                     (format-headers headers-map)
                     "\r\n")))
  ([status-code headers-map body]
   (let [msg-body (if (string? body) (str->bytes body) body)]
     (if (contains? 
           headers-map 
           "Content-Length") (concat 
                               (str->bytes
                                 (str (format-status-line 
                                        status-code)
                                      (format-headers 
                                        headers-map)
                                      "\r\n"))
                              msg-body)
       (compose status-code
                (merge headers-map
                       {"Content-Length" (count msg-body)})
                msg-body))))
)

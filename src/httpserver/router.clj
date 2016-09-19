(ns httpserver.router
  (:require [clojure.java.io :as io]))

(def routes
  {"GET" {"/redirect" [302 
                       {"Location" 
                        "http://localhost:5000/"}]
          "/coffee" [418
                     {}
                     "I'm a teapot"]
          "/tea" [200]
         }
   "PUT" {"/form" [200]} 
   "POST" {"/form" [200]}
   "OPTIONS" {"/method_options2" [200
                                  {"Allow"
                                   "GET,OPTIONS"}]
              "/method_options" [200
                                 {"Allow"
                                  "GET,HEAD,POST,OPTIONS,PUT"}]
              }
  }
) 

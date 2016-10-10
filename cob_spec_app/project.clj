(defproject cob_spec_app "0.1.0-SNAPSHOT"
  :description "Web app using HTTP server to meet cob_spec requirements"
  :url "http://github.com/hnlee/httpserver"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [httpserver "0.1.0-SNAPSHOT"]]
  :main ^:skip-aot cob-spec-app.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

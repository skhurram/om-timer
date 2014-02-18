(defproject paddleguru/timer "0.1.1"
  :source-paths ["src/clj" "src/cljs"]
  :min-lein-version "2.0.0"
  :uberjar-name "timer-standalone.jar"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [org.clojure/clojurescript "0.0-2156"]
                 [prismatic/dommy "0.1.1"]
                 [om "0.4.2"]
                 [kioo "0.2.0"]
                 [enfocus "2.0.2"]
                 [prismatic/schema "0.2.1"]
                 [ring "1.2.1"]
                 [compojure "1.1.6"]
                 [enlive "1.1.5"]]
  ;; When we put this into production, go with
  ;; https://github.com/paddleguru/paddleguru/blob/e50654c616f153f709283ca6012b8afd48af71fe/project.clj
  ;; for the advanced compilation settings
  :profiles {:uberjar {:prep-tasks [["cljsbuild" "clean"] ["cljsbuild" "once"]]}
             :dev {:repl-options {:init-ns timer.core}
                   :plugins [[com.cemerick/austin "0.1.3"]
                             [lein-cljsbuild "1.0.2"]]}}
  :cljsbuild {:builds [{:source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/cljs/dev/generated.js"
                                   :output-dir "resources/public/cljs/dev"
                                   :optimizations :whitespace
                                   :pretty-print true
                                   :source-map "resources/public/cljs/dev/generated.js.map"}}]})

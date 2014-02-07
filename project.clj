(defproject paddleguru/timer "0.1.1"
  :source-paths ["src/clj" "src/cljs"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2156"]
                 [prismatic/dommy "0.1.1"]
                 [kioo "0.1.0"]
                 [cljs-ajax "0.2.2"]
                 [enfocus "2.0.2"]
                 [prismatic/schema "0.2.0"]
                 [ring "1.2.1"]
                 [compojure "1.1.6"]
                 [enlive "1.1.5"]]
  :profiles {:dev {:repl-options {:init-ns timer.core}
                   :plugins [[com.cemerick/austin "0.1.3"]
                             [lein-cljsbuild "1.0.2"]]
                   :cljsbuild {:builds [{:source-paths ["src/cljs"]
                                         :compiler {:output-to "resources/public/cljs/dev/generated.js"
                                                    :output-dir "resources/public/cljs/dev"
                                                    :optimizations :whitespace
                                                    :pretty-print true
                                                    :source-map "resources/public/cljs/dev/generated.js.map"}}]}}})

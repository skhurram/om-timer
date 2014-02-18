(ns timer.core
  (:require [clojure.java.io :as io]
            [compojure.route :refer (resources)]
            [compojure.core :refer (GET defroutes)]
            [ring.adapter.jetty :as jetty]
            [net.cgrand.enlive-html :as html]))

(defmacro maybe-resolve [ns method]
    `(when-let [n# (find-ns (quote ~ns))]
       (when-let [m# (ns-resolve n# (quote ~method))]
         @m#)))

(defn austin-string []
  (when-let [f (maybe-resolve cemerick.austin.repls
                              browser-connected-repl-js)]
    (f)))

(html/deftemplate page
  (io/resource "public/index.html")
  [req]
  [:body] (html/append
           (html/html [:div {:id "react_shell"}]
                      [:script {:src "http://fb.me/react-0.8.0.js"
                                :type "text/javascript"}]
                      [:script {:src "/cljs/dev/generated.js"
                                :type "text/javascript"}]
                      [:script (austin-string)]
                      [:script "window.onload = paddleguru.client.timer.on_load;"])))

(defroutes site
  (GET "/timer" [] page)
  (resources "/"))

(defn -main
  ([] (-main "1234"))
  ([port]
     (let [port (Integer/parseInt port)]
       (defonce ^:private server
         (ring.adapter.jetty/run-jetty #'site {:port port :join? false}))
       server)))

;; ## Clojurescript REPL

(defn start! []
  (when-let [browser-repl-env (maybe-resolve cemerick.austin.repls browser-repl-env)]
    (when-let [cljs-repl (maybe-resolve cemerick.austin.repls cljs-repl)]
      (when-let [repl-env (maybe-resolve cemerick.austin repl-env)]
        (cljs-repl (reset! browser-repl-env
                           (repl-env)))))))

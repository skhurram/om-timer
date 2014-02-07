(ns timer.core
  (:require [cemerick.austin.repls :refer (browser-connected-repl-js)]
            [net.cgrand.enlive-html :as html]
            [compojure.route :refer (resources)]
            [compojure.core :refer (GET defroutes)]
            ring.adapter.jetty
            [clojure.java.io :as io]))

(def timing-template "public/html/timing.html")

(html/defsnippet make-timing-page
  timing-template
  [:div.content]
  [regatta-title]
  [:h4.event-name] (html/substitute nil)
  [:form] (html/set-attr :action "cake")
  [[:input (html/attr= :name "title")]] (html/set-attr :value regatta-title)
  [[:input (html/attr= :name "userid")]] (html/set-attr :value "userid")
  [[:input (html/attr= :name "event-numbers")]] (html/set-attr :value [1])
  [[:input (html/attr= :name "event-ids")]] (html/set-attr :value [1])
  [:td.event-column] (html/substitute "")
  [:td.roweventlabel]  (html/substitute ""))

(html/deftemplate page
  (io/resource "public/index.html")
  [req]
  [:body] (html/do->
           (html/content (make-timing-page (-> req :params :regatta-title)))
           (html/append
            (html/html [:div {:id "react_shell"}]
                       [:script {:src "http://fb.me/react-0.8.0.js"
                                 :type "text/javascript"}]
                       [:script {:src "/cljs/dev/generated.js"
                                 :type "text/javascript"}]
                       [:script (browser-connected-repl-js)]
                       [:script "window.onload = paddleguru.client.timer.on_load;"]))))

(defroutes site
  (resources "/")
  (GET "/races/:regatta-title/timing" [] page))

(defn run
  []
  (defonce ^:private server
    (ring.adapter.jetty/run-jetty #'site {:port 8080 :join? false}))
  server)

(defn start! []
  (cemerick.austin.repls/cljs-repl
   (reset! cemerick.austin.repls/browser-repl-env
           (cemerick.austin/repl-env))))

(ns chess.web
  (:use [ring.adapter.jetty :only [run-jetty]]
        [ring.util.response :only [response content-type]]
        [ring.middleware.resource :only [wrap-resource]]
        [ring.middleware.file-info :only [wrap-file-info]]
        [ring.middleware.reload :only [wrap-reload]]
        [compojure.core :only [defroutes GET]]
        [compojure.handler :only [api]]
        [hiccup.page :only [html5]]))

(def client-page
  (list
   [:head
    [:link {:rel "stylesheet" :type "text/css" :href "/css/chess.css"}]
    [:title "chess"]]
   [:body
    [:div#chess-board]
    [:script {:src "/js/chess.js" :type "text/javascript"}]]))

(defroutes chess-client
  (GET "/chess" []
       (-> (response (html5 client-page))
           (content-type "text/html"))))

(def webapp
  (-> chess-client
      (wrap-reload ["src"])
      (wrap-resource "public")
      wrap-file-info))

(defn -main [& args]
  (run-jetty (api #'webapp) {:port 8080 :join? true}))
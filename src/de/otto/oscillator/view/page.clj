(ns de.otto.oscillator.view.page
  (:require [compojure.core :as compojure]
            [de.otto.oscillator.view.layout :as layout]
            [de.otto.oscillator.view.component :as vc]))

(def build-in-pages
  {:detail {:url     "/detail"
            :heading ""
            :type    :detail}})

(defn- chart-def [chart-def-lookup-fun chart-name env]
  ((keyword chart-name) (chart-def-lookup-fun env)))

(defn- build-tile [tile page-config chart-def-lookup-fun url-params]
  (case (:type tile)
    :chart (let [chart-name (get-in tile [:params :chart-name])
                 chart-def (chart-def chart-def-lookup-fun chart-name (:env url-params))]
             (vc/link-to-chart page-config chart-def chart-name url-params))
    :image (vc/image (tile :params))
    :number (vc/number (tile :params))
    :plain-html (tile :params)))

(defn- build-page [page page-config chart-def-lookup-fun annotation-event-targets url-params]
  (case (:type page)
    :dashboard [:section
                (for [tile (:tiles page)]
                  (build-tile tile page-config chart-def-lookup-fun url-params))]
    :detail (let [chart-def (chart-def chart-def-lookup-fun (:chart url-params) (:env url-params))]
              [:section {:class "detail"}
               [:h2 (:chart url-params)]
               (vc/rickshaw-svg page-config chart-def annotation-event-targets url-params)
               (vc/plain-graphite-link page-config chart-def url-params)])))

(defn- page-route [page-config chart-def-lookup-fun annotation-event-targets [page-type page]]
  (compojure/GET (:url page) {params :params}
    (let [url-params (merge (:default-params page-config) params)
          content (build-page page page-config chart-def-lookup-fun annotation-event-targets url-params)]
      (layout/common :heading (:heading page)
                     :pages (:pages page-config)
                     :environments (:environments page-config)
                     :page-type page-type
                     :url-params url-params
                     :add-css-files (:add-css-files page-config)
                     :add-js-files (:add-js-files page-config)
                     :content content))))

(defn page-routes [page-config chart-def-lookup-fun annotation-event-targets]
  (map
    (partial page-route page-config chart-def-lookup-fun annotation-event-targets)
    (merge (:pages page-config) build-in-pages)))

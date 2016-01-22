(ns bought-and-sold.core
  (:require [net.cgrand.enlive-html :as html]))

(def base-url "https://research.seed.law.nyu.edu")

(defn pprint-output [data]
  (spit "resources/price-high-low.clj" (with-out-str (clojure.pprint/pprint data))))

(defn fetch-all-results [url]
  (html/html-resource (java.net.URL. url)))

(defn select-stuff [stub stuff]
  (html/select (fetch-all-results (str base-url stub)) stuff))

(defn fetch-all-cases [url]
  (map #(get-in % [:attrs :href])
       (select-stuff "/Search/Results" [:table.table :> :tr :td :a])))

(defn get-settlements [num-settlements]
  (pmap (fn [stub]
         [(str base-url stub) (->
                   (select-stuff stub [:div#bodyContent
                                       :div.headRoom
                                       [:div.newRow (html/nth-of-type 2)]
                                       :span.col-2-3])
                   first
                   html/text
                   clojure.string/trim)])
       (fetch-all-cases base-url)))

(defn filter-settlements [regex]
  (filter #(re-find regex (second %)) (get-settlements 5)))

(defn price-to-int []
  (map (fn [[url price]]
         [url (Integer.
               (apply str
                      (filter #(re-find #"\d" (str %)) (seq price))))])
       (filter-settlements #"\d")))

(defn sort-by-settlement []
  (reverse (sort-by #(second %) (price-to-int))))

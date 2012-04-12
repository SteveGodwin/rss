(ns rss
  (:gen-class)
  (:require [clojure.xml :as xml])
  (:require [clojure.set :as set])
  (:require [clojure.string :as str]))

(defn tag= [tagname]
  (fn [entry]
    (= (:tag entry) tagname)))

(defn tagged [tagname trees]
  (mapcat
   (fn [tree]
     (filter (tag= tagname) (:content tree)))
   trees))

(defn keys-from-rss [location]
  (->> (list (xml/parse location))
       (tagged :channel)
       (tagged :item)
       (tagged :key)
       (mapcat :content)))

(defn added [old new]
  (set/difference new old))

(defn removed [old new]
  (set/difference old new))

(defn -main [& args]
  (def old (set (keys-from-rss (first args))))
  (def new (set (keys-from-rss (fnext args))))
  (println "Added: " (str/join " " (added old new)))
  (println "Removed: " (str/join " " (removed old new))))
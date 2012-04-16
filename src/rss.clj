(ns rss
  (:gen-class)
  (:require [clojure.xml :as xml])
  (:require [clojure.set :as set])
  (:require [clojure.zip :as zip])
  (:require [clojure.string :as str])
  (:use [clojure.data.zip.xml]))

(defn parse-xml [location]
  (zip/xml-zip (xml/parse location)))

(defn added [old new]
  (set/difference new old))

(defn removed [old new]
  (set/difference old new))

(defn item-key [item]
  (xml1-> item :key text))

(defn insane? [item]
  (let [time    (xml1-> item :timespent (attr :seconds))
        points  (xml1-> item :customfields :customfield
                        [:customfieldname "Story Points"]
                        :customfieldvalues text)
        kids (count (xml-> item :subtasks :subtask))]
    (and (or (nil? points)
             (= (int (Float/valueOf points)) 0))
         (or (nil? time)
             (= (int (Float/valueOf time)) 0))
         (= kids 0))))

(defn sanity-check [items]
  (let [insane-items (map item-key (filter insane? items))]
    (cond (not (empty? insane-items))
          (println "Items with no time, points or children: "
                   (str/join " " insane-items)))))
            
(defn -main [& args]
  (def old-items (xml-> (parse-xml (first args)) :channel :item))
  (def new-items (xml-> (parse-xml (fnext args)) :channel :item))

  (def old-set (set (map item-key old-items)))
  (def new-set (set (map item-key new-items)))

  (println "Added: " (str/join " " (added old-set new-set)))
  (println "Removed: " (str/join " " (removed old-set new-set)))
  (sanity-check new-items))
(ns rss
  (:gen-class)
  (:require [clojure.xml :as xml])
  (:require [clojure.set :as set])
  (:require [clojure.zip :as zip])
  (:require [clojure.string :as str])
  (:use [clojure.data.zip.xml]))

(defn parse-xml [location]
  (zip/xml-zip (xml/parse location)))

(defn added   [old new] (set/difference new old))
(defn removed [old new] (set/difference old new))

(defn item-key      [item] (xml1-> item :key text))
(defn item-time     [item] (xml1-> item :timespent (attr :seconds)))
(defn item-title    [item] (xml1-> item :title text))
(defn item-subtasks [item] (xml-> item :subtasks :subtask))

(defn item-points [item]
  (xml1-> item :customfields :customfield
          [:customfieldname "Story Points"]
          :customfieldvalues text))

(defn bug? [item]
  (= "Bug" (xml1-> item :type text)))

(defn insane? [item]
  (let [time   (item-time item)
        points (item-points item)]
    (and (or (nil? points)
             (= (int (Float/valueOf points)) 0))
         (or (nil? time)
             (= (int (Float/valueOf time)) 0))
         (not (bug? item))
         (= 0 (count (item-subtasks item))))))

(defn print-keys [message items]
  (println (str/join "\n" (cons message items))))

(defn -main [& args]
  (def old-items (xml-> (parse-xml (first args)) :channel :item))
  (def new-items (xml-> (parse-xml (fnext args)) :channel :item))
  (def old-set (set (map item-key old-items)))
  (def new-set (set (map item-key new-items)))

  (print-keys "Added:" (added old-set new-set))
  (print-keys "Removed:" (removed old-set new-set))
  (print-keys "Insane:" (map item-title (filter insane? new-items)))
  (print-keys "Bugs:" (map item-title (filter bug? new-items))))
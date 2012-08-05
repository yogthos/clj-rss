(ns clj-rss.core(:use [clojure.xml :only [emit]]
                      [clojure.set :only [difference]])
  (:require [clojure.xml :as xml])
  (:import java.util.Date java.text.SimpleDateFormat))

(defn- format-time [t]
  (when t
    (.format (new SimpleDateFormat "EEE, dd MMM yyyy HH:mm:ss ZZZZ") t)))

(defn- xml-str [s]
  (if s
    (-> s (.replace "&" "&amp;") (.replace "<" "&lt;") (.replace ">" "&gt;"))
    ""))

(defmacro tag [id & xs]
  `(let [attrs# (map? (first '~xs))
         content# [~@xs]]          
     {:tag ~id 
      :attrs (if attrs# (first '~xs)) 
      :content (if attrs# (rest content#) content#)}))

(defmacro functionize [macro]
  `(fn [& args#] (eval (cons '~macro args#))))

(defmacro apply-macro [macro args]
   `(apply (functionize ~macro) ~args))


(defn- validate-tags [tags valid-tags]
  (let [diff (difference (set tags) valid-tags)]     
    (when (not-empty diff)
      (throw (new Exception (str "unrecognized tags in channel " (apply str diff)))))))

(defn- validate-channel [tags & ks]
  (doseq [k ks]
    (or (get tags k) (throw (new Exception (str k " is a required element")))))
  (validate-tags (keys tags)
                 #{:title 
                   :link 
                   :description 
                   :category 
                   :cloud 
                   :copyright 
                   :docs 
                   :image 
                   :language 
                   :lastBuildDate 
                   :managingEditor 
                   :pubDate 
                   :rating 
                   :skipDays 
                   :skipHours                     
                   :ttl 
                   :webMaster}))

(defn- validate-item [tags]
  (if (not (or (:title tags) (:description tags)))
    (throw (new Exception (str "item " tags " must contain one of title or description!"))))
  (validate-tags (keys tags) #{:title :link :description :author :category :comments :enclosure :guid :pubDate :source}))

(defn- make-tags [tags] 
  (for [[k v] (seq tags)]
    (if (coll? v)      
      (apply-macro tag (into [k] v))
      (tag k (cond 
               (some #{k} [:pubDate :lastBuildDate]) (format-time v)
               (some #{k} [:description :title]) (xml-str v)             
               :else v)))))


(defn- item [tags] 
  (validate-item tags)
  {:tag :item
   :attrs nil
   :content (make-tags tags)})

(defn channel
  "channel accepts a map of tags followed by 0 or more items

  channel:
  required tags: title, link, description
  optional tags: category, cloud, copyright, docs, image, language, lastBuildDate, managingEditor, pubDate, rating, skipDays, skipHours, textInput, ttl, webMaster

  item:
  optional tags: title, link, description, author, category, comments, enclosure, guid, pubDate, source
  one of title or description is required!

  tags can either be strings, dates, or collections
  
  :title \"Some title\"
  {:tag :title :attrs nil :content [\"Some title\"]}

  :category [{:domain \"http://www.foo.com/test\"} \"TEST\"]
  {:tag :category
   :attrs {:domain \"http://www.fool.com/cusips\"}
   :content (\"MSFT\")}

  official RSS specification: http://cyber.law.harvard.edu/rss/rss.html"
  [tags & items]
  (validate-channel tags :title :link :description)
  (with-out-str
    (emit
      {:tag :rss
       :attrs {:version "2.0"}
       :content
       [{:tag :channel
         :attrs nil
         :content (concat (make-tags (conj tags {:generator "clj-rss"})) (map item items))}]})))


(ns clj-rss.core
  (:require [clojure.data.xml :refer [cdata emit-str]]
            [clojure.set :refer [difference]]
            [clojure.string :refer [join]])
  (:import (java.time Instant ZoneOffset)
           java.time.format.DateTimeFormatter
           java.util.Locale))

(defn- format-time [^Instant t]
  (when t
    (.format (DateTimeFormatter/ofPattern "EEE, dd MMM yyyy HH:mm:ss ZZ" Locale/ENGLISH)
             (.atOffset t ZoneOffset/UTC))))

(defn- xml-str
  "Returns a value suitable for inclusion as an XML element. If the string
  is wrapped in <![CDATA[ ... ]]>, remove the tags and wrap in a CData record"
  [^String s]
  (if (and (.startsWith s "<![CDATA[")
           (.endsWith s "]]>"))
    (-> s
        (.replace "<![CDATA[" "")
        (.replace "]]>" "")
        cdata)
    s))

(defmacro tag [id & xs]
  `(let [attrs# (map? (first '~xs))
         content# [~@xs]]
     {:tag     ~id
      :attrs   (if attrs# (first '~xs))
      :content (if attrs# (rest content#) content#)}))

(defmacro functionize [macro]
  `(fn [& args#] (eval (cons '~macro args#))))

(defmacro apply-macro [macro args]
  `(apply (functionize ~macro) ~args))

(defn dissoc-nil
  "Returns a map containing only those entries in m whose val is not nil"
  [map]
  (let [non-nil-keys (for [[k v] map :when (not (nil? v))] k)]
    (select-keys map non-nil-keys)))

(defn- validate-tags [tags valid-tags]
  (let [diff (difference (set tags) valid-tags)]
    (when (not-empty diff)
      (throw (new Exception (str "unrecognized tags in channel: " (join ", " diff)))))))

(defn- validate-channel [tags & ks]
  (doseq [k ks]
    (or (get tags k) (throw (new Exception (str k " is a required element")))))
  (validate-tags (keys tags)
                 #{:title
                   :link
                   :feed-url
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
  (when (not (or (:title tags) (:image tags) (:description tags)))
    (throw (new Exception (str "item " tags " must contain one of title or description!"))))
  (when (and (get tags "content:encoded") (not (:description tags)))
    (throw (new Exception (str "item " tags " must contain a description since it contains a content:enclosed!"))))
  (validate-tags (keys tags) #{:type :image :url :title :link :description "content:encoded" :author :category :comments :enclosure :guid :pubDate :source}))



(defn- make-tags [tags]
  (flatten
   (for [[k v] (seq tags)]
     (cond
       (and (coll? v) (map? (first v)))
       (apply-macro clj-rss.core/tag (into [k] v))
       (coll? v)
       (map (fn [v] (make-tags {k v})) v)
       :else
       (let [v (cond
                 (some #{k} [:pubDate :lastBuildDate]) (format-time v)
                 (some #{k} [:description :title :link :author "content:encoded"]) (xml-str v)
                 :else v)]
          (cond
            (= k :guid)
            (tag k {:isPermaLink "false"} v)
            :else
            (tag k v)))))))


(defn- item [validate? tags]
  (when validate? (validate-item (dissoc-nil tags)))
  (let [;;"content:encoded" must come after "description"
        content (get tags "content:encoded")
        ordered (-> tags (dissoc "content:encoded") (assoc "content:encoded" content))]
    {:tag (or (:type tags) :item)
     :attrs nil
     :content (make-tags (dissoc-nil (dissoc ordered :type)))}))

(defn- channel'
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
  [validate? tags & items]
  (when validate? (validate-channel tags :title :link :description))
  {:tag   :rss
   :attrs {:version     "2.0"
           "xmlns:atom" "http://www.w3.org/2005/Atom"
           "xmlns:content""http://purl.org/rss/1.0/modules/content/"}
   :content
   [{:tag     :channel
     :attrs   nil
     :content (concat
               [{:tag   "atom:link"
                 :attrs {:href (or (:feed-url tags) (:link tags))
                         :rel  "self"
                         :type "application/rss+xml"}}]
               (make-tags (-> tags (dissoc :feed-url) (conj {:generator "clj-rss"})))
               (->> items
                    flatten
                    (map dissoc-nil)
                    (map (partial item validate?))))}]})

(defn channel [& content]
  (cond
    (every? #(if (coll? %) (empty? %) (nil? %)) content)
    (channel' false nil)
    (map? (first content))
    (apply channel' (cons true content))
    :else
    (apply channel' content)))

(defn channel-xml
  "channel accepts a map of tags followed by 0 or more items and outputs an XML string, see channel docs for detailed description"
  [& content]
  (emit-str (apply channel content)))

(ns clj-rss.core
  (:use [clojure.xml :only [emit]]
        [clojure.set :only [difference]]
        [clojure.string :only [escape join]])
  (:import java.util.Date java.text.SimpleDateFormat))

(defn- format-time [t]
  (when t
    (.format (new SimpleDateFormat "EEE, dd MMM yyyy HH:mm:ss ZZZZ") t)))

(defn- xml-str [s]
  (if s
    (let [escapes {\< "&lt;",
                   \> "&gt;",
                   \& "&amp;",
                   \" "&quot;"}]
      (escape s escapes))))

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

(defn dissoc-nil [map]
  "Returns a map containing only those entries in m whose val is not nil"
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
      (apply-macro clj-rss.core/tag (into [k] v))
      (tag k (cond
               (some #{k} [:pubDate :lastBuildDate]) (format-time v)
               (some #{k} [:description :title]) (xml-str v)
               :else v)))))


(defn- item [validate? tags]
  (if validate? (validate-item (dissoc-nil tags)))
  {:tag :item
   :attrs nil
   :content (make-tags (dissoc-nil tags))})

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
  {:tag :rss
   :attrs {:version "2.0"
           "xmlns:atom" "http://www.w3.org/2005/Atom"}
   :content
   [{:tag :channel
     :attrs nil
     :content (concat
                [{:tag "atom:link"
                  :attrs {:href (:link tags)
                          :rel "self"
                          :type "application/rss+xml"}}]
                (make-tags (conj tags {:generator "clj-rss"}))
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
  (with-out-str (emit (apply channel content))))

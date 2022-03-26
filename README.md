# clj-rss

A library for generating RSS feeds from Clojure

[![Continuous Integration status](https://secure.travis-ci.org/yogthos/clj-rss.png)](http://travis-ci.org/yogthos/clj-rss)

## Installation

Leiningen

[![Clojars Project](http://clojars.org/clj-rss/latest-version.svg)](http://clojars.org/clj-rss)

**note**: clj-rss uses `org.clojure/data.xml` version 0.2.0-alpha6.

## Usage

The `channel-xml` function accepts a map of tags representing a channel, followed by 0 or more maps for items (or a seq of items) and outputs an XML string.
Each item must be a map of valid RSS tags.

The following characters in the content of :description, "content:encoded" and :title tags will be escaped: `<`, `&`, `>`, `"`. Both `:pubDate` and `:lastBuildDate` keys are expected to be instances
of `java.time.Instant` or one of its subclasses. These will be converted to standard RSS date strings in the resulting XML.

If you need to get the data in a structured format, use `channel` instead.

The project works with babashka so you can generate rss feeds as a script.
See examples/babashka directory.

### Examples

Creating a channel with some items:
```clojure
(require '[clj-rss.core :as rss])

(rss/channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                 {:title "Foo"}
                 {:title "post" :author "author@foo.bar"}
                 {:description "bar"}
                 {:description "baz" "content:encoded" "Full content"})
```

image tags can be inserted by providing the `:type` key:
```clojure
(channel-xml {:title "Foo" :link "http://x" :description "some channel"}
             {:type  :image
              :title "image"
              :url   "http://foo.bar"
              :link  "http://bar.baz"}
             {:title "foo" :link "bar"})
```

Creating a feed from a sequence of items:
```clojure
(let [items [{:title "Foo"} {:title "Bar"} {:title "Baz"}]]
  (rss/channel {:title "Foo" :link "http://foo/bar" :description "some channel"}
               items))

;; Atom feed URL can be specified using :feed-url key:
(rss/channel
  {:title "foo" :link "http://foo" :feed-url "http://feed-url" :description "bar"}
  {:type  :image
   :title "Title"
   :url   "http://bar"
   :link  "http://baz"})
```

Creating items with complex tags:
```clojure
(rss/channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                 {:title "test"
                  :category [{:domain "http://www.foo.com/bar"} "BAZ"]})

(rss/channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                 {:title "test"
                  :category [[{:domain "http://www.microsoft.com"} "MSFT"]
                             [{:domain "http://www.apple.com"} "AAPL"]]})

(rss/channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
              {:title "test"
              :category ["MSFT" "AAPL"]})
```

Items can contain raw HTML if the tag is enclosed in `<![CDATA[ ... ]]>`:
```clojure
  {:title "HTML Item"
   :description "<![CDATA[ <h1><a href='http://foo/bar'>Foo</a></h1> ]]>"
   "content:encoded" "<![CDATA[ <article><h1>Title</h1><p>Hello World</p></article> ]]>"}
```

To get the raw data structure use:
```clojure
(rss/channel {:title "Foo" :link "http://foo/bar" :description "some channel"}
             {:title "test"})
```

Pass in `false` as first parameter to disable content validation:
```clojure
(rss/channel-xml false {:title "Foo" :link "http://foo/bar" :description "some channel"}
                       {:title "test"
                        :category [{:domain "http://www.foo.com/bar"} "BAZ"]})

(rss/channel false {:title "Foo" :link "http://foo/bar" :description "some channel"}
                   {:title "test"})
```

The output XML can be validated at http://validator.w3.org/feed/#validate_by_input

For more information on valid RSS tags and their content please refer to the official RSS 2.0 specification http://cyber.law.harvard.edu/rss/rss.html

## License

Copyright Yogthos 2012

Distributed under the Eclipse Public License, the same as Clojure.

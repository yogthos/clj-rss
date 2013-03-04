# clj-rss

A library for generating RSS feeds from Clojure

[![Continuous Integration status](https://secure.travis-ci.org/yogthos/clj-rss.png)](http://travis-ci.org/yogthos/clj-rss)

## Installation

Leiningen

```clojure
[clj-rss "0.1.2"]
```

## Usage

`channel-xml` function which accepts a map of tags representing a channel followed by 0 or more maps for items and outputs an XML string. 
Each item must be a map of valid RSS tags. 

Following characters in the content of :description and :title tags will be escaped: `<`, `&`, `>`, `"`. Both `:pubDate` and `:lastBuildDate` keys are expected to be instances of `java.util.Date` 
or one of its subclasses. These will be converted to standard RSS date strings in the resulting XML.


If you need to get the data in a structured format use `channel` instead.
 
### Examples

Creating a channel with a some items

```clojure
(require '[clj-rss.core :as rss])

(rss/channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                 {:title "Foo"}
                 {:title "post" :author "author@foo.bar"}
                 {:description "bar"})
```

Creating items with complex tags:
```clojure
(rss/channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                 {:title "test"
                  :category [{:domain "http://www.foo.com/bar"} "BAZ"]})
```

to get the raw data structure use:
```clojure
(rss/channel {:title "Foo" :link "http://foo/bar" :description "some channel"}
             {:title "test"})
```

pass in `false` as first parameter to disable content validation:
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

# clj-rss

A library for generating RSS feeds from Clojure

## Usage

The library provides a `channel` function which accepts a map of channel tags followed by 0 or more items. 
Each item must be a map of valid RSS tags. 
Following characters in the Content of :description and :title tags will be escaped: <, &, >
pubDate and lastBuildDate must be instances of java.util.Date

### Examples

Creating a channel with a some items

```Clojure
(channel {:title "Foo" :link "http://foo/bar" :description "some channel"}
                 {:title "Foo"}
                 {:title "post" :author "author@foo.bar"}
                 {:description "bar"})
```

Creating items with complex tags:
```Clojure
(channel {:title "Foo" :link "http://foo/bar" :description "some channel"}
                 {:title "test"
                  :category [{:domain "http://www.foo.com/bar"} "BAZ"]})
```

The output XML can be validated at http://validator.w3.org/feed/#validate_by_input

For more information on valid RSS tags and their content please refer to the official RSS 2.0 specification http://cyber.law.harvard.edu/rss/rss.html

## License

Copyright © 2012 FIXME

Distributed under the Eclipse Public License, the same as Clojure.

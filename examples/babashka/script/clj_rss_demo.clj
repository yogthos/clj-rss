(ns clj-rss-demo
  (:require [clj-rss.core :as rss]))


(defn -main [& _args]
  (println (rss/channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                            {:title "Foo"}
                            {:title "post" :author "author@foo.bar"}
                            {:description "bar"}
                            {:description "baz" "content:encoded" "Full content"})))
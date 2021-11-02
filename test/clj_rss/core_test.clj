(ns clj-rss.core-test
  (:use clojure.test
        clj-rss.core
        hickory.core))

(deftest proper-message
  (is
    (= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:content=\"http://purl.org/rss/1.0/modules/content/\"><channel><atom:link href=\"http://foo/bar\" rel=\"self\" type=\"application/rss+xml\"/><title>Foo</title><link>http://foo/bar</link><description>some channel</description><generator>clj-rss</generator><item><title>Foo</title></item><item><title>post</title><author>Yogthos</author></item><item><description>bar</description></item></channel></rss>"
       (channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                    {:title "Foo"}
                    {:title "post" :author "Yogthos"}
                    {:description "bar"})
       (channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                    {:title "Foo" :link nil :enclosure nil}
                    {:title "post" :author "Yogthos"}
                    {:description "bar"})
       (channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                    [{:title "Foo"}
                     {:title "post" :author "Yogthos"}
                     {:description "bar"}]))))


(deftest escaping-test
  (is (=
        {:tag     :rss,
         :attrs   {:version "2.0", "xmlns:atom" "http://www.w3.org/2005/Atom", "xmlns:content" "http://purl.org/rss/1.0/modules/content/"},
         :content [{:tag     :channel,
                    :attrs   nil,
                    :content [{:tag "atom:link", :attrs {:href "http://foo", :rel "self", :type "application/rss+xml"}}
                              {:content ["foo"], :attrs nil, :tag :title}
                              {:content ["http://foo"], :attrs nil, :tag :link}
                              {:content ["bar"], :attrs nil, :tag :description}
                              {:content ["clj-rss"], :attrs nil, :tag :generator}
                              {:tag     :image,
                               :attrs   nil,
                               :content [{:content [#clojure.data.xml.node.CData{:content " title "}], :attrs nil, :tag :title}
                                         {:content ["<![CDATA[ url ]]>"], :attrs nil, :tag :url}
                                         {:content [#clojure.data.xml.node.CData{:content " link "}], :attrs nil, :tag :link}]}]}]}

        (channel
          {:title "foo" :link "http://foo" :description "bar"}
          {:type  :image
           :title "<![CDATA[ title ]]>"
           :url   "<![CDATA[ url ]]>"
           :link  "<![CDATA[ link ]]>"}))))

(deftest image-tag
  (is
    (= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:content=\"http://purl.org/rss/1.0/modules/content/\"><channel><atom:link href=\"http://x\" rel=\"self\" type=\"application/rss+xml\"/><title>Foo</title><link>http://x</link><description>some channel</description><generator>clj-rss</generator><image><title>image</title><url>http://foo.bar</url><link>http://bar.baz</link></image><item><title>foo</title><link>bar</link></item></channel></rss>"
       (channel-xml {:title "Foo" :link "http://x" :description "some channel"}
                    {:type  :image
                     :title "image"
                     :url   "http://foo.bar"
                     :link  "http://bar.baz"}
                    {:title "foo" :link "bar"}))))

(deftest feed-url
  (is
    (=
      {:tag     :rss
       :attrs   {:version        "2.0"
                 "xmlns:atom"    "http://www.w3.org/2005/Atom"
                 "xmlns:content" "http://purl.org/rss/1.0/modules/content/"}
       :content [{:tag     :channel
                  :attrs   nil
                  :content '({:tag "atom:link" :attrs {:href "http://foo" :rel "self" :type "application/rss+xml"}}
                             {:content ["foo"] :attrs nil :tag :title}
                             {:content ["http://foo"] :attrs nil :tag :link}
                             {:content ["bar"] :attrs nil :tag :description}
                             {:content ["clj-rss"] :attrs nil :tag :generator}
                             {:tag     :image
                              :attrs   nil
                              :content ({:content ["Title"], :attrs nil :tag :title}
                                        {:content ["http://bar"] :attrs nil :tag :url}
                                        {:content ["http://baz"] :attrs nil :tag :link})})}]}
      (channel
        {:title "foo" :link "http://foo" :description "bar"}
        {:type  :image
         :title "Title"
         :url   "http://bar"
         :link  "http://baz"})))
  (is
    (=
      {:tag     :rss
       :attrs   {:version        "2.0"
                 "xmlns:atom"    "http://www.w3.org/2005/Atom"
                 "xmlns:content" "http://purl.org/rss/1.0/modules/content/"}
       :content [{:tag     :channel
                  :attrs   nil
                  :content '({:tag "atom:link" :attrs {:href "http://feed-url" :rel "self" :type "application/rss+xml"}}
                             {:content ["foo"] :attrs nil :tag :title}
                             {:content ["http://foo"] :attrs nil :tag :link}
                             {:content ["bar"] :attrs nil :tag :description}
                             {:content ["clj-rss"] :attrs nil :tag :generator}
                             {:tag     :image
                              :attrs   nil
                              :content ({:content ["Title"] :attrs nil :tag :title}
                                        {:content ["http://bar"] :attrs nil :tag :url}
                                        {:content ["http://baz"] :attrs nil :tag :link})})}]}
      (channel
        {:title "foo" :link "http://foo" :feed-url "http://feed-url" :description "bar"}
        {:type  :image
         :title "Title"
         :url   "http://bar"
         :link  "http://baz"}))))

(deftest invalid-channel-tag
  (is
    (thrown? Exception
             (channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel" :baz "invalid-tag"}
                          {:title "Foo"}
                          {:title "post" :author "Yogthos"}
                          {:description "bar"}))))

(deftest missing-channel-tag
  (is
    (thrown? Exception
             (channel-xml {:title "Foo" :link "http://foo/bar"}
                          {:title "Foo"}))))

(deftest invalid-item-tag
  (is
    (thrown? Exception
             (channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                          {:title "Foo" :invalid-tag "foo"}))))

(deftest missing-item-tag
  (is
    (thrown? Exception
             (channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                          {:link "http://foo"}))))

(deftest item-with-a-guid-tag
  (is
   (=
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:content=\"http://purl.org/rss/1.0/modules/content/\"><channel><atom:link href=\"http://foo/bar\" rel=\"self\" type=\"application/rss+xml\"/><title>Foo</title><link>http://foo/bar</link><description>some channel</description><generator>clj-rss</generator><item><title>test</title><guid isPermaLink=\"false\">http://foo/bar</guid></item></channel></rss>"
    (channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                 {:title "test"
                  :guid "http://foo/bar"}))))

(deftest complex-tag
  (is (= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:content=\"http://purl.org/rss/1.0/modules/content/\"><channel><atom:link href=\"http://foo/bar\" rel=\"self\" type=\"application/rss+xml\"/><title>Foo</title><link>http://foo/bar</link><description>some channel</description><generator>clj-rss</generator><item><title>test</title><category domain=\"http://www.fool.com/cusips\">MSFT</category></item></channel></rss>"
         (channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                      {:title    "test"
                       :category [{:domain "http://www.fool.com/cusips"} "MSFT"]}))))

(deftest cdata-tag
  (is (= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:content=\"http://purl.org/rss/1.0/modules/content/\"><channel><atom:link href=\"http://foo/bar\" rel=\"self\" type=\"application/rss+xml\"/><title>Foo</title><link>http://foo/bar</link><description>some channel</description><generator>clj-rss</generator><item><title>HTML Item</title><description><![CDATA[ <h1><a href=\"http://foo/bar\">Foo</a></h1> ]]></description></item></channel></rss>"
         (channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                      {:title "HTML Item" :description "<![CDATA[ <h1><a href=\"http://foo/bar\">Foo</a></h1> ]]>"}))))

(deftest validation-on
  (is
    (thrown? Exception
             (channel-xml true
                          {:title "Foo" :description "Foo" :link "http://foo/bar"}
                          {:foo "Foo"}))))

(deftest validation-off
  (is (= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:content=\"http://purl.org/rss/1.0/modules/content/\"><channel><atom:link href=\"http://foo/bar\" rel=\"self\" type=\"application/rss+xml\"/><title>Foo</title><description>Foo</description><link>http://foo/bar</link><generator>clj-rss</generator><item><foo>Foo</foo></item></channel></rss>"
         (channel-xml false
                      {:title "Foo" :description "Foo" :link "http://foo/bar"}
                      {:foo "Foo"}))))

(deftest test-dissoc-nil
  (is (= {:title "Foo" :description "Bar"}
         (dissoc-nil {:title "Foo" :description "Bar"
                      :link  nil :category nil}))))

(deftest content-encoded-comes-after-description
  (is (= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:content=\"http://purl.org/rss/1.0/modules/content/\"><channel><atom:link href=\"http://foo/bar\" rel=\"self\" type=\"application/rss+xml\"/><title>Foo</title><link>http://foo/bar</link><description>some channel</description><generator>clj-rss</generator><item><title>test</title><description>short</description><content:encoded>LONG CONTENT</content:encoded></item></channel></rss>"
         (channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                      {:title "test" :description "short" "content:encoded" "LONG CONTENT"})
         (channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                      {:title "test" "content:encoded" "LONG CONTENT" :description "short"}))))

(deftest missing-description-when-content-given
  (is (thrown? Exception
               (channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                            {:title "test" "content:encoded" "LONG CONTENT"}))))

(deftest format-time-supports-instant
  (is (= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:content=\"http://purl.org/rss/1.0/modules/content/\"><channel><atom:link href=\"http://foo/bar\" rel=\"self\" type=\"application/rss+xml\"/><title>Foo</title><link>http://foo/bar</link><description>some channel</description><lastBuildDate>Thu, 01 Jan 1970 00:00:00 +0000</lastBuildDate><generator>clj-rss</generator><item><title>Foo</title><pubDate>Thu, 01 Jan 1970 00:00:00 +0000</pubDate></item></channel></rss>"
         (channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel" :lastBuildDate (java.time.Instant/ofEpochSecond 0)}
                      {:title "Foo" :pubDate (java.time.Instant/ofEpochSecond 0)}))))

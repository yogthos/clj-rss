(ns clj-rss.core-test
  (:use clojure.test
        clj-rss.core
        hickory.core))

(deftest proper-message
  (is
   (= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\"><channel><atom:link href=\"http://foo/bar\" rel=\"self\" type=\"application/rss+xml\"/><title>Foo</title><link>http://foo/bar</link><description>some channel</description><generator>clj-rss</generator><item><title>Foo</title></item><item><title>post</title><author>Yogthos</author></item><item><description>bar</description></item></channel></rss>"
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

(deftest complex-tag
  (is (= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\"><channel><atom:link href=\"http://foo/bar\" rel=\"self\" type=\"application/rss+xml\"/><title>Foo</title><link>http://foo/bar</link><description>some channel</description><generator>clj-rss</generator><item><title>test</title><category domain=\"http://www.fool.com/cusips\">MSFT</category></item></channel></rss>"
         (channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                      {:title "test"
                       :category [{:domain "http://www.fool.com/cusips"} "MSFT"]}))))

(deftest cdata-tag
  (is (= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\"><channel><atom:link href=\"http://foo/bar\" rel=\"self\" type=\"application/rss+xml\"/><title>Foo</title><link>http://foo/bar</link><description>some channel</description><generator>clj-rss</generator><item><title>HTML Item</title><description><![CDATA[ <h1><a href=\"http://foo/bar\">Foo</a></h1> ]]></description></item></channel></rss>"
         (channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                      {:title "HTML Item" :description "<![CDATA[ <h1><a href=\"http://foo/bar\">Foo</a></h1> ]]>"}))))

(deftest validation-on
  (is
   (thrown? Exception
            (channel-xml true
                         {:title "Foo" :description "Foo" :link "http://foo/bar"}
                         {:foo "Foo"}))))

(deftest validation-off
  (is (= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\"><channel><atom:link href=\"http://foo/bar\" rel=\"self\" type=\"application/rss+xml\"/><title>Foo</title><description>Foo</description><link>http://foo/bar</link><generator>clj-rss</generator><item><foo>Foo</foo></item></channel></rss>"
         (channel-xml false
                      {:title "Foo" :description "Foo" :link "http://foo/bar"}
                      {:foo "Foo"}))))

(deftest test-dissoc-nil
  (is (= {:title "Foo" :description "Bar"}
         (dissoc-nil {:title "Foo" :description "Bar"
                      :link nil :category nil}))))

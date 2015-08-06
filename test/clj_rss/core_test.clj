(ns clj-rss.core-test
  (:use clojure.test
        clj-rss.core
        hickory.core))

(deftest proper-message
  (is
   (= "<?xml version='1.0' encoding='UTF-8'?>\n<rss version='2.0' xmlns:atom='http://www.w3.org/2005/Atom'>\n<channel>\n<atom:link href='http://foo/bar' rel='self' type='application/rss+xml'/>\n<generator>\nclj-rss\n</generator>\n<description>\nsome channel\n</description>\n<title>\nFoo\n</title>\n<link>\nhttp://foo/bar\n</link>\n<item>\n<title>\nFoo\n</title>\n</item>\n<item>\n<title>\npost\n</title>\n<author>\nYogthos\n</author>\n</item>\n<item>\n<description>\nbar\n</description>\n</item>\n</channel>\n</rss>\n"
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
  (is (= "<?xml version='1.0' encoding='UTF-8'?>\n<rss version='2.0' xmlns:atom='http://www.w3.org/2005/Atom'>\n<channel>\n<atom:link href='http://foo/bar' rel='self' type='application/rss+xml'/>\n<generator>\nclj-rss\n</generator>\n<description>\nsome channel\n</description>\n<title>\nFoo\n</title>\n<link>\nhttp://foo/bar\n</link>\n<item>\n<category domain='http://www.fool.com/cusips'>\nMSFT\n</category>\n<title>\ntest\n</title>\n</item>\n</channel>\n</rss>\n"
         (channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                      {:title "test"
                       :category [{:domain "http://www.fool.com/cusips"} "MSFT"]}))))

(deftest cdata-tag
  (is (= "<?xml version='1.0' encoding='UTF-8'?>\n<rss version='2.0' xmlns:atom='http://www.w3.org/2005/Atom'>\n<channel>\n<atom:link href='http://foo/bar' rel='self' type='application/rss+xml'/>\n<generator>\nclj-rss\n</generator>\n<description>\nsome channel\n</description>\n<title>\nFoo\n</title>\n<link>\nhttp://foo/bar\n</link>\n<item>\n<description>\n<![CDATA[ <h1><a href='http://foo/bar'>Foo</a></h1> ]]>\n</description>\n<title>\nHTML Item\n</title>\n</item>\n</channel>\n</rss>\n"
         (channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                      {:title "HTML Item" :description "<![CDATA[ <h1><a href='http://foo/bar'>Foo</a></h1> ]]>"}))))

(deftest validation-on
  (is
   (thrown? Exception
            (channel-xml true
                         {:title "Foo" :description "Foo" :link "http://foo/bar"}
                         {:foo "Foo"}))))

(deftest validation-off
  (is (= "<?xml version='1.0' encoding='UTF-8'?>\n<rss version='2.0' xmlns:atom='http://www.w3.org/2005/Atom'>\n<channel>\n<atom:link href='http://foo/bar' rel='self' type='application/rss+xml'/>\n<generator>\nclj-rss\n</generator>\n<description>\nFoo\n</description>\n<title>\nFoo\n</title>\n<link>\nhttp://foo/bar\n</link>\n<item>\n<foo>\nFoo\n</foo>\n</item>\n</channel>\n</rss>\n"
         (channel-xml false
                      {:title "Foo" :description "Foo" :link "http://foo/bar"}
                      {:foo "Foo"}))))

(deftest test-dissoc-nil
  (is (= {:title "Foo" :description "Bar"}
         (dissoc-nil {:title "Foo" :description "Bar"
                      :link nil :category nil}))))

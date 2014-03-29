(ns clj-rss.core-test
  (:use clojure.test
        clj-rss.core))

(deftest proper-message
  (is
    (= "<?xml version='1.0' encoding='UTF-8'?>\n<rss version='2.0'>\n<channel>\n<generator>\nclj-rss\n</generator>\n<link>\nhttp://foo/bar\n</link>\n<title>\nFoo\n</title>\n<description>\nsome channel\n</description>\n<item>\n<title>\nFoo\n</title>\n</item>\n<item>\n<author>\nYogthos\n</author>\n<title>\npost\n</title>\n</item>\n<item>\n<description>\nbar\n</description>\n</item>\n</channel>\n</rss>\n"
       (channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                {:title "Foo"}
                {:title "post" :author "Yogthos"}
                {:description "bar"}))))

#_(deftest empty-content
  (is (= {:tag :rss, :attrs {:version "2.0"}, :content [{:tag :channel, :attrs nil, :content []}]}
         (channel [])))

  (is (= "<?xml version='1.0' encoding='UTF-8'?>\n<rss version='2.0'>\n<channel>\n</channel>\n</rss>\n"
       (channel-xml []))))


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
  (is (= "<?xml version='1.0' encoding='UTF-8'?>\n<rss version='2.0'>\n<channel>\n<generator>\nclj-rss\n</generator>\n<link>\nhttp://foo/bar\n</link>\n<title>\nFoo\n</title>\n<description>\nsome channel\n</description>\n<item>\n<title>\ntest\n</title>\n<category domain='http://www.fool.com/cusips'>\nMSFT\n</category>\n</item>\n</channel>\n</rss>\n"
         (channel-xml {:title "Foo" :link "http://foo/bar" :description "some channel"}
                  {:title "test"
                   :category [{:domain "http://www.fool.com/cusips"} "MSFT"]}))))

(deftest validation-on
  (is
    (thrown? Exception
             (channel-xml true
                          {:title "Foo" :description "Foo" :link "http://foo/bar"}
                          {:foo "Foo"}))))

(deftest validation-off
  (is(= "<?xml version='1.0' encoding='UTF-8'?>\n<rss version='2.0'>\n<channel>\n<generator>\nclj-rss\n</generator>\n<link>\nhttp://foo/bar\n</link>\n<title>\nFoo\n</title>\n<description>\nFoo\n</description>\n<item>\n<foo>\nFoo\n</foo>\n</item>\n</channel>\n</rss>\n"
        (channel-xml false
                     {:title "Foo" :description "Foo" :link "http://foo/bar"}
                     {:foo "Foo"}))))

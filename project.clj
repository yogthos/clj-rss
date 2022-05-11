(defproject clj-rss "0.4.0"
  :description "A library for generating RSS feeds from Clojure."
  :url "https://github.com/yogthos/clj-rss"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/data.xml "0.0.8"]]
  :profiles {:dev
             {:dependencies [[hickory "0.7.1"]]}})

(defproject kwrooijen/yggdrasil "0.0.1-SNAPSHOT"
  :description "Server side rendering with Reagent and websocket atoms"
  :url "https://github.com/kwrooijen/yggdrasil"
  :license {:name "MIT"}
  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  :dependencies [[reagent "0.10.0"]
                 [meta-merge "1.0.0"]
                 [buddy/buddy-core "1.6.0"]
                 [com.taoensso/sente "1.16.0-alpha1"]
                 ;; Mirror of hiccup master. Once https://github.com/weavejester/hiccup/pull/167
                 ;; is released we can use that.
                 [kwrooijen/hiccup "0.0.0-SNAPSHOT"]]
  :plugins [[lein-cloverage "1.1.2"]
            [lein-codox "0.10.7"]
            [lein-ancient "0.6.15"]]
  :codox {:doc-files ["README.md"]
          :output-path "docs/"
          :html {:namespace-list :nested}
          :metadata {:doc/format :markdown}
          :themes [:rdash]}
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.1"]
                                  [org.clojure/clojurescript "1.10.764"]
                                  [orchestra "2020.07.12-1"]
                                  [codox-theme-rdash "0.1.2"]]}
             :test {:dependencies [[orchestra "2020.07.12-1"]]}}
  :deploy-repositories [["releases" :clojars]])

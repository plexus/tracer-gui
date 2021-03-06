(defproject tracer-gui "0.1.0-SNAPSHOT"
  :description "Simple GUI for inspecting Ring-like requests"
  :url "https://github.com/plexus"
  :license {:name "Mozilla Public License 2.0"
            :url "https://www.mozilla.org/en-US/MPL/2.0/"}
  :dependencies [[org.clojure/clojure "1.7.0"]]
  :plugins []
  :source-paths ["src"]
  :test-paths ["test"]
  :aot [tracer-gui.gui])

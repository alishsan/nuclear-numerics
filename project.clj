(defproject org.clojars.alishsan/nuclear-numerics "0.1.0-SNAPSHOT"
  :description "Numerical methods library for nuclear physics calculations: Numerov integration, coupled channels, and nuclear potentials"
  :url "https://github.com/your-username/nuclear-numerics"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [generateme/fastmath "3.0.0-alpha4-SNAPSHOT" :exclusions [com.github.haifengl/smile-mkl]]]
  :deploy-repositories [["clojars" {:url "https://repo.clojars.org"
                                     :sign-releases false}]]
  :profiles {:dev {:dependencies [[org.clojure/test.check "1.1.1"]]}})

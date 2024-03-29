(ns mikrobloggeriet.olorm-cli-test
  (:require [mikrobloggeriet.olorm-cli :as olorm-cli]
            [clojure.test :refer [deftest testing is]]))

;; Certain shell script tests don't work under CI.
(def ci? (= "runner" (System/getenv "USER")))

(deftest repo-path-test
  (testing "A repo path is set"
    (is (some? (olorm-cli/repo-path)))))

(deftest create-opts->commands-test
  (testing "we can generate commands without errors"
    (is (some? (olorm-cli/create-opts->commands {:dir (olorm-cli/repo-path) :git false :edit false}))))

  (testing "A seq of commands is returned, all commands are prints"
    (is (every? keyword?
                (->> (olorm-cli/create-opts->commands {:dir (olorm-cli/repo-path) :git false :edit false})
                     (map first)))))

  (testing "When we transform to a dry run, only printable commands are returned"
    (is (every? #{:prn :println}
                (->> (olorm-cli/create-opts->commands {:dir (olorm-cli/repo-path)
                                                       :git false
                                                       :edit false})
                     (map olorm-cli/command->dry-command)
                     (map first)))))

  (testing "When git is enabled, there is shelling out"
    (is (contains? (->> (olorm-cli/create-opts->commands {:dir (olorm-cli/repo-path) :git true :edit false})
                        (map first)
                        (into #{}))
                   :shell)))

  (when-not ci? ; EDITOR is not set on CI
    (testing "When edit is enabled, there is shelling out"
      (is (contains? (->> (olorm-cli/create-opts->commands {:dir (olorm-cli/repo-path) :git false :edit true})
                          (map first)
                          (into #{}))
                     :shell)))))

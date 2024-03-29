;; JALS CLI

(ns mikrobloggeriet.jals-cli
  (:require
   [babashka.cli :as cli]
   [babashka.fs :as fs]
   [clojure.edn :as edn]
   [clojure.string :as str]
   [mikrobloggeriet.jals :as jals]
   [babashka.process :refer [shell]]))

;; TODO -- trekk ut logikk og lagre under "mikrobloggeriet".
;; (senere)
(defn config-folder [] (str (fs/xdg-config-home) "/olorm"))
(defn config-file [] (str (config-folder) "/config.edn"))

(defn repo-path []
  (-> (config-file)
      slurp
      edn/read-string
      :repo-path))

(declare subcommands)

(defn jals-help [{:keys [_opts]}]
  (println "Available subcommands:")
  (println)
  (doseq [{:keys [cmds]} subcommands]
    (when (and (seq cmds) (not= cmds ["lol"]))
      (println (str "  jals " (str/join " " cmds))))))

(defn jals-set-repo-path [{:keys [opts]}]
  (let [repo-path (:repo-path opts)]
    (when-not repo-path
      (println (str/trim "
Usage:

    olorm set-repo-path REPO_PATH

REPO_PATH is the path to the OLORM repo. The CLI can be used from any folder on
your system, so we need to know where to find OLORM pages.
"))
      (System/exit 1))
    (let [repo-path (fs/absolutize repo-path)]
      (fs/create-dirs (config-folder))
      (spit (config-file) (prn-str {:repo-path (str repo-path)})))))

(defn jals-repo-path [{:keys [_opts]}]
  (if (fs/exists? (config-file))
    (println (repo-path))
    (do
      (println "Error: config file not found")
      (System/exit 1))))

(defn jals-create [{:keys [opts]}]
  (when (or (:help opts) (:h opts))
    (println (str/trim "
Usage:

  $ jals create [OPTION...]

Allowed options:

  --disable-git-commands  Disable all Git commands. Useful for testing.
  --disable-git-magic     Alias for --disable-git-commands
  --dry-run               Supress side effects and print commands instead
  --help                  Show this helptext.
  --no-git-commands  Disable all Git commands. Useful for testing.
  --no-git-magic          Alias for --disable-git-commands
"))
    (System/exit 0))
  (let [repo-path (repo-path)
        dispatch (fn [cmd & args]
                   (if (:dry-run opts)
                     (prn `(~cmd ~@args))
                     (apply (resolve cmd) args)))
        disable-git-commands (or (:disable-git-commands opts)
                                 (:no-git-magic opts)
                                 (:disable-git-magic opts)
                                 (= false (:git-magic opts))
                                 (= false (:git-commands opts)))]
    (when-not disable-git-commands
      (dispatch `shell {:dir repo-path} "git pull --ff-only"))
    (let [next-number (inc (or (->> (jals/docs {:repo-path repo-path}) (map :number) sort last)
                               0))
          doc (jals/->jals {:repo-path repo-path :number next-number})
          next-jals-dir (jals/path doc)]
      (dispatch `fs/create-dirs next-jals-dir)
      (let [next-index-md (jals/index-md-path doc)]
        (dispatch `spit next-index-md (jals/md-skeleton doc))
        (dispatch `spit (jals/meta-path doc) (prn-str {:git.user/email (jals/git-user-email {:repo-path repo-path})
                                                       :doc/created (jals/today)
                                                       :doc/uuid (jals/uuid)}))
        (dispatch `shell {:dir repo-path} (System/getenv "EDITOR") next-index-md)
        (when-not disable-git-commands
          (dispatch `shell {:dir repo-path} "git add .")
          (dispatch `shell {:dir repo-path} "git commit -m" (str "jals-" (:number doc)))
          (dispatch `shell {:dir repo-path} "git pull --rebase") ;; pull & rebase if someone is writing another another microblog entry
          (dispatch `shell {:dir repo-path} "git push")))
      (let [jals-announce-nudge (str "Husk å publisere i #mikrobloggeriet-announce på Slack. Feks:"
                                     "\n\n"
                                     (str "   JALS-" (:number doc) ": $DIN_TITTEL → https://mikrobloggeriet.no/j/" (:slug doc) "/"))]
        (println jals-announce-nudge)))))

(defn jals-draw [{:keys [opts]}]
  (let [pool (:pool opts)]
    (when (or (:h opts)
              (:help opts)
              (not pool))
      (println (str/trim "
Usage:

  $ jals draw POOL

POOL is a string that can contain the first letters of the JALS authors.
Example usage:

  $ jals draw jals
  Adrian
"
                         ))
      (if (or (:h opts) (:help opts))
        (System/exit 0)
        (System/exit 1)))
    (prn
     (get (zipmap "jals" '(Jørgen Adrian Lars Sindre))
          (rand-nth pool)))))

(def subcommands
  [
   {:cmds ["create"]        :fn jals-create}
   {:cmds ["help"]          :fn jals-help}
   {:cmds ["repo-path"]     :fn jals-repo-path}
   {:cmds ["set-repo-path"] :fn jals-set-repo-path :args->opts [:repo-path]}
   {:cmds ["draw"]          :fn jals-draw          :args->opts [:pool]}
   {:cmds []                :fn jals-help}])

(defn -main [& args]
  (cli/dispatch subcommands args))

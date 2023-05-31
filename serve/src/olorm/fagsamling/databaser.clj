;; # Relasjonell data i Datascript og SQLite
;;
;; Agenda:
;;
;; 1. Motivasjon: https://roamresearch.com/#/app/teod/page/hZaGT2_7K
;;
;; 2. Intro til Clojure.
;;    Data, verdier.
;;    https://learnxinyminutes.com/docs/clojure/
;;
;;    Mål: dekke data-literals, og grunnleggende funksjoner fra data til data.
;;    Fint om jeg får hjelp her!
;;    Still spørsmål!
;;
;; 3. Datamodellering i Clojure.
;;    SQLite (SQL) og Datascript.

;; ---

(ns olorm.fagsamling.databaser
  (:require
   [next.jdbc :as jdbc]
   [nextjournal.clerk :as clerk]))

;; Vi kan kalle en Java-funksjon for å lage UUID-er:

(defn uuid []
  (str (java.util.UUID/randomUUID)))

;; Den bruker vi sånn:

(uuid)

;; Sånn kan vi modellere personer med navn i SQLite.
;;
;; `:memory:` betyr en in-memory-database som ikke lagres. Sånne er fine til
;; feks enhesttester.

(let [db (jdbc/get-datasource {:dbtype "sqlite" :dbname ":memory:"})]
  (with-open [conn (jdbc/get-connection db)]
    (jdbc/execute! conn
                   [(str "CREATE TABLE IF NOT EXISTS person"
                         " ( uuid string UNIQUE, navn string )")])
    (jdbc/execute! conn
                   ["INSERT INTO person (uuid, navn) VALUES (?, ?)"
                    (uuid) "Teodor :)"])
    (jdbc/execute! conn
                   ["INSERT INTO person (uuid, navn) VALUES (?, ?)"
                    (uuid) "sindreeeeee"])
    (clerk/table (jdbc/execute! conn
                                ["SELECT * FROM person"]))))

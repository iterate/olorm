;; # Relasjonell data i Datascript og SQLite

(ns olorm.fagsamling.databaser
  (:require
   [next.jdbc :as jdbc]
   [nextjournal.clerk :as clerk]))

;; Vi kan kalle en Java-funksjon for å lage UUID-er:

(defn uuid []
  (str (java.util.UUID/randomUUID)))

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

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

^{:nextjournal.clerk/toc true}
(ns olorm.fagsamling.databaser
  (:require
   [next.jdbc :as jdbc]
   [nextjournal.clerk :as clerk]
   [datascript.core :as datascript]))

;; Vi kan kalle en Java-funksjon for å lage UUID-er:

(defn uuid []
  (str (java.util.UUID/randomUUID)))

;; Den bruker vi sånn:

(uuid)

;; ## Personer med SQLite
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

;; ## Personer med Datascript

(let [schema {:person/name {}
              :person/uuid {}}
      conn   (datascript/create-conn schema)]
  (datascript/transact! conn [{:person/name "Teodor :)" :person/uuid (uuid)}
                              {:person/name "sindreeee" :person/uuid (uuid)}])
  (datascript/q '[:find ?navn ?uuid
                  :where
                  [?entitet :person/name ?navn]
                  [?entitet :person/uuid ?uuid]]
                @conn))

;; nå får vi et "sett av tupler" tilbake.
;; hvis vi vil, kan vi lage tabell i stedet:

(let [schema {:person/name {}
              :person/uuid {}}
      conn   (datascript/create-conn schema)]
  (datascript/transact! conn [{:person/name "Teodor :)" :person/uuid (uuid)}
                              {:person/name "sindreeee" :person/uuid (uuid)}])
  (clerk/table (for [[n u]
                     (datascript/q '[:find ?navn ?uuid
                                     :where
                                     [?entitet :person/name ?navn]
                                     [?entitet :person/uuid ?uuid]]
                                   @conn)]
                 {"Navn" n "UUID" u})))

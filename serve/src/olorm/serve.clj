(ns olorm.serve
  (:require
   [compojure.core :refer [defroutes GET]]
   [hiccup.page :as page]
   [iki.api :as iki]
   [mikrobloggeriet.olorm :as olorm]
   [mikrobloggeriet.jals :as jals]
   [clojure.pprint]
   [org.httpkit.server :as httpkit]))

(defn shared-html-header
  "Shared header content -- for example CSS imports."
  []
  [[:meta {:charset "utf-8"}]
   (hiccup.page/include-css "/vanilla.css")
   (hiccup.page/include-css "/mikrobloggeriet.css")])

(defn feeling-lucky []
  [:a {:href "/random-doc" :class :feeling-lucky} "🎲"])

(defn index [_req]
  (let [mikrobloggeriet-announce-url "https://garasjen.slack.com/archives/C05355N5TCL"
        github-olorm-url "https://github.com/iterate/olorm/"
        _tech-forum-url "https://garasjen.slack.com/archives/C2K35RDNJ"
        teodor-url "https://teod.eu/"
        hops-url "https://www.headless-operations.no/"
        iterate-url "https://www.iterate.no/"]
    (page/html5
     (into [:head] (shared-html-header))
     [:body
      [:p (feeling-lucky)]
      [:h1 "Mikrobloggeriet"]
      [:p "Teknologer fra Iterate deler fra hverdagen."]
      [:h2 "OLORM"]
      [:p "OLORM skrives av Oddmund, Lars og Richard."]
      [:p
       (interpose " · "
                  (for [olorm (olorm/docs {:repo-path ".."})]
                    [:a {:href (olorm/href olorm)} (:slug olorm)]))]
      [:h2 "JALS"]
      [:p "JALS skrives av Jørgen, Adrian, Lars og Sindre."]
      [:p
       (interpose " · "
                  (for [doc (jals/docs {:repo-path ".."})]
                    [:a {:href (jals/href doc)} (:slug doc)]))]

      [:hr]

      [:section
       [:h2 "Hva er dette for noe?"]
       [:p
        "Mikrobloggeriet er et initiativ der teknologer fra " [:a {:href iterate-url} "Iterate"] " deler ting de bryr seg om i hverdagen. "
        "Vi velger å publisere fritt tilgjengelig på Internett fordi vi har tro på å dele kunnskap. "
        "Innhold og kode for Mikrobloggeriet på " [:a {:href github-olorm-url} "github.com/iterate/olorm"] ". "
        "Mikrobloggeriet kjører på " [:a {:href hops-url} "Headless Operations"] ". "
        "Hvis du har spørsmål eller kommentarer, kan du ta kontakt med " [:a {:href teodor-url} "Teodor"] "."]]

      [:section
       [:h2 "Er det mulig å diskutere publiserte dokumenter?"]
       [:p "Vi oppfordrer alle til å kommentere og diskutere!"
        " Men vi tror det er lettest å gjøre på Slack."
        " Delta gjerne i diskusjonen i tråd på "
        [:a {:href mikrobloggeriet-announce-url} "#mikrobloggeriet-announce"]
        "!"]]

      [:section
       [:h2 "Jeg er Iterate-teknolog og vil skrive, hva gjør jeg?"]
       [:p "Finn deg 2-3 andre å skrive med, og snakk med Teodor."
        " Vi setter av en time der vi går gjennom skriveprosessen og installerer tooling."
        " Deretter får dere en \"prøveuke\" der dere kan prøve dere på å skrive cirka hver tredje dag."
        " Så kan dere bestemme dere for om dere vil fortsette å skrive eller ikke."]]
      ])))

(defn olorm-index [_req]
  (page/html5
   (into [:head] (shared-html-header))
   [:body
    [:p
     (feeling-lucky)
     " — "
     [:a {:href "/"} "mikrobloggeriet"]]
    [:h1 "Alle OLORM-er"]
    [:table
     [:thead [:td "slug"] [:td "author"] [:td "created"]]
     [:tbody
      (for [olorm (map olorm/load-meta (olorm/docs {:repo-path ".."}))]
        [:tr
         [:td [:a {:href (olorm/href olorm)} (:slug olorm)]]
         [:td (olorm/author-name olorm)]
         [:td (:doc/created olorm)]])]]]))

(defn jals-index [_req]
  (page/html5
   (into [:head] (shared-html-header))
   [:body
    [:p
     (feeling-lucky)
     " — "
     [:a {:href "/"} "mikrobloggeriet"]]
    [:h1 "Alle JALS-er"]
    [:table
     [:thead [:td "slug"] [:td "author"] [:td "created"]]
     [:tbody
      (for [jals (map jals/load-meta (jals/docs {:repo-path ".."}))]
        [:tr
         [:td [:a {:href (jals/href jals)} (:slug jals)]]
         [:td (jals/author-name jals)]
         [:td (:doc/created jals)]])]]]))

(def markdown->html
  "Convert markdown to html with pandoc and an in-memory cache"
  (iki/cache-fn-by iki/markdown->html identity))

(defn olorm->html [olorm]
  (when (olorm/exists? olorm)
    (markdown->html (slurp (olorm/index-md-path olorm)))))

(defn olorm [req]
  (tap> req)
  (let [olorm (olorm/->olorm {:slug (:slug (:route-params req))
                              :repo-path ".."})
        {:keys [number]} olorm]
    (tap> (olorm/->olorm olorm))
    {:status (if olorm 200 404)
     :body
     (page/html5
      (into [:head] (shared-html-header))
      [:body
       [:p
        (feeling-lucky)
        " — "
        [:a {:href "/"} "mikrobloggeriet"]
        " "
        [:a {:href "/o/"} "o"]
        " — "
        [:span (interpose " · " (filter some?
                                        [(let [prev (olorm/->olorm {:number (dec number) :repo-path ".."})]
                                           (when (olorm/exists? prev)
                                             [:a {:href (olorm/href prev)} (:slug prev)]))
                                         [:span (:slug olorm)]
                                         (let [prev (olorm/->olorm {:number (inc number) :repo-path ".."})]
                                           (when (olorm/exists? prev)
                                             [:a {:href (olorm/href prev)} (:slug prev)]))]))]]
       (olorm->html olorm)])}))

(defn jals->html [doc]
  (when (jals/exists? doc)
    (markdown->html (slurp (jals/index-md-path doc)))))

(defn jals [req]
  (let [doc (jals/->doc {:slug (:slug (:route-params req))
                         :repo-path ".."})
        {:keys [number]} doc
        html (jals->html doc)]
    {:status (if html 200 404)
     :body
     (page/html5
      (into [:head] (shared-html-header))
      [:body
       [:p (feeling-lucky)
        " — "
        [:a {:href "/"} "mikrobloggeriet"]
        " "
        [:a {:href "/j/"} "j"]
        " — "
        [:span (interpose " · " (filter some?
                                        [(let [prev (jals/->doc {:number (dec number) :repo-path ".."})]
                                           (when (jals/exists? prev)
                                             [:a {:href (jals/href prev)} (:slug prev)]))
                                         [:span (:slug doc)]
                                         (let [prev (jals/->doc {:number (inc number) :repo-path ".."})]
                                           (when (jals/exists? prev)
                                             [:a {:href (jals/href prev)} (:slug prev)]))]))]
        ]
       html])}))

(defn random-doc [_req]
  (let [target (or
                 (when-let [docs (into [] cat [(olorm/docs {:repo-path ".."})
                                               (jals/docs {:repo-path ".."})])]
                   (let [picked (rand-nth docs)]
                     (cond
                       (= :olorm (:cohort picked)) (olorm/href picked)
                       (= :jals (:cohort picked)) (jals/href picked)
                       :else nil)))
                 "/")]
    {:status 307 ;; temporary redirect
     :headers {"Location" target}
     :body ""}))

(comment
  (olorm/random {:repo-path ".."})
  (jals/random {:repo-path ".."})

  )

(defroutes app
  (GET "/" req (index req))
  (GET "/health" _req {:status 200 :headers {"Content-Type" "text/plain"} :body "all good!"})
  (GET "/vanilla.css" _req {:status 200 :headers {"Content-Type" "text/css"} :body (slurp "vanilla.css")})
  (GET "/mikrobloggeriet.css" _req {:status 200 :headers {"Content-Type" "text/css"} :body (slurp "mikrobloggeriet.css")})
  (GET "/o/" req (olorm-index req))
  (GET "/j/" req (jals-index req))
  (GET "/o/:slug/" req (olorm req))
  (GET "/j/:slug/" req (jals req))
  (GET "/random-doc" _req random-doc))

(defonce server (atom nil))
(def port 7223)
(defn stop-server [stop-fn] (when stop-fn (stop-fn)) nil)
#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn stop! [] (swap! server stop-server))
#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn start! [_opts]
  (swap! server (fn [old-server]
                  (stop-server old-server)
                  (println (str "olorm.serve running: http://localhost:" port))
                  (httpkit/run-server #'app {:port port}))))

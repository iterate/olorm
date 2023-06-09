# OLORM-7: Gjør det vondt? Lag en subkommando.

Hei!

Jeg (i dag Teodor) synes det er viktig å ta eierskap til egen arbeidsprosess for oss som jobber med utvikling.
Ofte ender jeg opp med å bygge meg et CLI for å gjøre ting lett å jobbe med.
I dag vil jeg dele et konkret eksempel.

Hopp forbi `Digresjon: litt Clojure-koding` hvis du bare vil lese konklusjonen.

## Digresjon: litt Clojure-koding

Dette gjorde jeg i praksis i dag.
Fram til nå har jeg trukket neste OLORM-forfatter sånn:

1. Les oppover i olorm-intern etter hvem jeg trakk forrige gang

2. Skriv en ny Clojure-kodesnutt for å trekke neste OLORM-forfatter.
   For eksempel, hvis forrige OLORM-trekking var følgende:

    ```clj
    $ bb -e "(rand-nth '(oddmund lars richard))"
    oddmund
    ```
    
    Så kan neste trekking bli sånn:
    
    ```clj
    $ bb -e "(rand-nth '(lars richard))"
    richard
    ```
    
    (når vi "går tom", fyller vi på med Lars, Richard og Oddmund i trekke-sekken igjen)
    
I dag synes jeg dette var kjipt.
Det var mye skriving.
Så jeg bestemte meg å ta eierskap for det som gjorde vondt ved å skrive en subkommando.

Jeg startet med å skrive ned hvordan jeg ville subkommandoen skulle fungere:

```shell
$ olorm draw olr
richard
```

Det er viktig for meg å tenke på hvilken oppførsel jeg ønsker _før_ jeg begynner å kode.
Ellers føler jeg at jeg bare surrer rundt.
Og når jeg har _ett eksempel_ på hva jeg vil at skal funke, kan jeg implementere akkurat det, uten å skrive masse kode jeg må kaste.

Jeg starter med å lage en ny subkommando som gjør at jeg kan se hva jeg driver med:

```shell
$ git diff f693631..3abc617
diff --git a/cli/src/olorm/cli.clj b/cli/src/olorm/cli.clj
index 27da7d5..3a1f0e1 100644
--- a/cli/src/olorm/cli.clj
+++ b/cli/src/olorm/cli.clj
@@ -77,12 +77,21 @@ Allowed options:
           (shell {:dir repo-path} "git commit -m" (str "olorm-" (:number olorm)))
           (shell {:dir repo-path} "git push"))))))

+(defn olorm-draw [{:keys [opts]}]
+  (let [pool (:pool opts)]
+    (prn `(str/blank? ~pool)
+         (str/blank? pool))
+    (prn `(rand-nth ~pool)
+         (rand-nth pool))
+    ))
+
 (def subcommands
   [
    {:cmds ["create"]        :fn olorm-create}
    {:cmds ["help"]          :fn olorm-help}
    {:cmds ["repo-path"]     :fn olorm-repo-path}
    {:cmds ["set-repo-path"] :fn olorm-set-repo-path :args->opts [:repo-path]}
+   {:cmds ["draw"]          :fn olorm-draw          :args->opts [:pool]}
    {:cmds []                :fn olorm-help}])

 (defn -main [& args]
```

Den kan jeg bruke sånn:

```shell
$ olorm draw olr
(clojure.string/blank? "olr") false
(clojure.core/rand-nth "olr") \l
$ olorm draw
(clojure.string/blank? nil) true
(clojure.core/rand-nth nil) nil
```

(digresjon: jeg synes REPL i Clojure er fantastisk, men når jeg skriver CLI-er foretrekker jeg å jobbe direkte med CLI-et)

Så:

1. `clojure.string/blank?` kan si meg om jeg har en tom tekststreng, og gir samme svar når den får inn `""` og `nil`.
   (`nil` i Clojure er som `null` eller `undefined` i Javascript)
2. Men `rand-nth` bare gir meg `nil` hvis jeg prøver å trekke fra en tom liste.
   (og Clojure later som at `nil` er en "tom collection av passende type". Dette fenomenet kalles [nil-punning])

[nil-punning]: https://ericnormand.me/podcast/what-is-nil-punning

Jeg tenker å trekke personens navn fra et map sånn:

```clojure
user=> (get (zipmap "olr" '(oddmund lars richard)) \r)
richard
```

[zipmap] tar inn to "ting som ser ut som lister" og lager en mapping fra elementer i den venstre lista til elementer i den høyre lista.

[zipmap]: https://clojuredocs.org/clojure.core/zipmap

Så jeg implementerer kommandoen sånn:

```clojure
(defn olorm-draw [{:keys [opts]}]
  (let [pool (:pool opts)]
    (prn
     (get (zipmap "olr" '(oddmund lars richard))
          (rand-nth pool)))))
```

Meeen, det er vanskelig å lære hvordan man bruker kommandoen.

```shell
$ olorm draw olr
oddmund
$ olorm draw
nil
```

Så jeg vil ha hjelpetekst.
Da skriver jeg et eksempel på hjelpeteksten jeg ønsker:

```shell
$ olorm draw
[skal returnere exit-kode 1]
???
```

Jeg ser på en hjelpetekst som finnes:

```shell
Usage:

  olorm create [OPTION...]

Allowed options:

  --help               Show this helptext.
  --disable-git-magic  Disable running any Git commands. Useful for testing.
```

OK, nå var det lettere.

```shell
$ olorm draw
Usage:

  olorm create POOL

POOL is a string that can contain the first letters of the OLORM authors.
Example usage:

  $ olorm draw olr
  Richard
```

Nå har jeg skrevet helptext som funker (mener jeg) i rett kontekst.

Så jeg implementerer helptext i kommandoen:

```clj
(defn olorm-draw [{:keys [opts]}]
  (let [pool (:pool opts)]
    (when (or (:h opts)
              (:help opts)
              (not pool))
      (println (str/trim "
Usage:

  $ olorm create POOL

POOL is a string that can contain the first letters of the OLORM authors.
Example usage:

  $ olorm draw olr
  Richard
"
                         ))
      (if (or (:h opts) (:help opts))
        (System/exit 0)
        (System/exit 1)))
    (prn
     (get (zipmap "olr" '(oddmund lars richard))
          (rand-nth pool)))))
```

Den kan brukes sånn:

```shell
$ olorm draw
Usage:

  $ olorm create POOL

POOL is a string that can contain the first letters of the OLORM authors.
Example usage:

  $ olorm draw olr
  Richard
```

... og kommandoen kan brukes neste gang:

```shell
$ olorm draw olr
lars
```

meeen det teller ikke, vi skal trekke på ekte i morgen.

## Oppsummering

1. Når noe gjør vondt i utviklingsprosessen min, prøver jeg å løse det ved å lage meg CLI-er jeg kan bruke.
2. Jeg tillater meg selv å kose meg litt når jeg gjør det.
   Jeg mener også det er en del av jobben vår å sørge for god developer experience når vi koder.
3. Hvis ingen tar ansvar for ergonomien i det vi koder, kommer det til å bli bare dritt.
   Så er vi i gang.
   [The Pragmatic Programmer] har et kapittel som heter "Don't leave broken windows" om dette.
   
[The Pragmatic Programmer]: https://play.teod.eu/the-pragmatic-programmer/

## Retrospektiv

1. OLORM-er skal ta 5-10 minutter å skrive.
   Jeg sprengte tidsskjemaet med cirka ti-gangeren.
   Dette setter dårlig presedens, og jeg innser at det er _kjempevanskelig_ å vise "en liten bit uten å bruke masse tid."
2. Det var litt gøy å skrive :)
3. Jeg er glad vi har et CLI for OLORM så vi kan fikse småting som "å trekke OLORM-forfatter er kjedelig"
4. Hvis man skal skrive så langt som dette hver gang, er hver tredje dag alt for ofte.
   Jeg kunne kanskje satt av tid til en sånn en hver uke. Eller annenhver uke.
   Men jeg kunne jo også gjort noe "mindre".
   Feks trukket en tilfeldig [personlig aforisme], og forklart hva jeg legger i den.

[personlig aforisme]: https://play.teod.eu/aphorisms/

## Referanser

GeePaw Hill er en flink fyr som også snakker om hvordan vi utviklere kan ta kontroll over arbeidsprosessen vår.
Hvis du synes denne artikkelen var spennnede, vil du kanskje like [podcasten hans][geepaw-podcast].

[geepaw-podcast]: https://www.geepawhill.org/tag/podcast/

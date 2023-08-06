# OLORM-33: Et første møte med CICD

> Alle kan Github actions, ikke sant?
> Og YAML, det må da folk ha hørt om.

Nope!
Vi utviklere må lære nye ting hver dag.
Det er en fantastisk mulighet, og en ganske stor byrde.
Alle tingene vi kan i dag har vi lært på et tidspunkt, og alle tingene vi ønsker å kunne gjøre i framtiden må vi gjøre oss en innsats for å lære.

## Status på koden i Mikrobloggeriet

Når jeg har jobbet med Mikrobloggeriet-koden har jeg fulgt noen prinsipper:

1. Vi demonstrerer ekte bruk før vi koder masse.
   Hvis ingen vil skrive, skal vi ikke bruke tid på å kode.
2. I koden skal vi _vente med abstraksjoner_.
   Vi vil heller ha få filer som gjør for mye, for så og trekke ut senere, enn det motsatte.

Jeg synes det har fungert bra til nå.
Men det har gitt noen utfordringer:

1. Vi lagde mikroblogg med 41 innlegg før vi skrev en eneste enhetstest.
2. Da den første andre personen enn meg skulle prøve å spinne opp koden, ble det trøbbel.

Da er det på tide å sakke ned!
Jeg ville da få til følgende:

1. Kodebasen legger opp til at man kan skrive tester og jobbe mot testene lokalt.
2. Testene kjører mot hver commit på Github (CI)
3. Vi prodsetter automatisk kode når testene er grønne (CICD)
4. Kodebasen inneholder ingen uferdig eller ubrukt kode ("clean code")

## Noen refleksjoner om CICD

**Utrolig fint å kunne lene seg på grønne tester i commit-loggen.**
Det gir meg ro!

**Testene kjører dobbelt!**
Jeg kjører både testene gjennom Github Actions og i selve bygget (i Docker).
Det føltes litt rart å velge det, er ikke dette duplisering?
Jeg vil kjøre testene i en GH Action fordi da får jeg god tilbakemelding på _hva, spesifikt_ som feiler.
Og jeg kjører testene mine i Docker i bygget for å unngå at koden blir prodsatt hvis testene feiler.
Jeg kunne kanskje sagt at "prodsetting skal vente på at alle sjekker er ferdig".
Men jeg synes det jeg har funker helt fint, og nå vet jeg i tillegg at alle ganger jeg bygger med Docker lokalt er testene grønne.

**Github Actions og YAML er noe man må lære seg!**
Her er en start:

- For å starte med GH actions, legg til én fil: `.github/workflows/test.yml`

- Her er et minimalt grønt eksempel:

  ```yaml
  name: Run tests
  on: [push, pull_request]
  jobs:
  Testing:
      runs-on: ubuntu-latest
      steps:
      - run: echo success!
  ```
  
  Hvis du legger til denne i repoet ditt, bør du se en liten grønn prikk ved siden av commit-ene dine.
  
- Her er et minmalt rødt eksempel:

  ```yaml
  name: Run tests
  on: [push, pull_request]
    jobs:
      Testing:
        runs-on: ubuntu-latest
        steps:
          - run: FAIL
  ```
  
  Eksempelet feiler fordi `FAIL` ikke er en systemkommando.
  Alle stegene i en jobb kjører systemkommandoer.

## Er testing og CICD verdt det?

Min konklusjon så langt er _ja_.
Hvis man ikke har god testdekning og kontroll på hvilke commits som er grønne og røde, blir det utrygt å skrive kode.
Og når det er utrygt å endre kode, er det vanskeligere å komme framover på produktet.

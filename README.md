# Mikrobloggeriet

> **THIS REPOSITORY HAS MOVED**: https://github.com/iterate/mikrobloggeriet/

https://mikrobloggeriet.no/

_A collection of technical microblogs in Norwegian._
Previously known as OLORM.

Vocabulary:

| term            | definition                                             | Deprecated? |
|-----------------|--------------------------------------------------------|-------------|
| cohort          | a microblogging collective                             |             |
| doc             | a microblog entry                                      |             |
| mikrobloggeriet | a collection of microblogs                             |             |
| olorm           | a CLI for microblogging                                |             |
| olorm           | a collection of microblogs                             | Deprecated. |
| olorm           | Oddmund, Lars and Richard's microblog collective       |             |
| jals            | Jørgen, Adrian, Lars and Sindre's microblog collective |             |
| jals            | a CLI for microblogging                                |             |

Note: the "olorm" term is now ambiguous.
It used to be the name of the whole, now it's only a _part_ of the whole.
But [Cool URIs don't change], and I don't want to break existing links.
So we keep the repo name for now.
All old URIs will continue to work.

[Cool URIs don't change]: https://www.w3.org/Provider/Style/URI

## Oppsett før installasjon av kommandolinjeprogram

Skal du bruke et kommandolinjeprogram for å skrive på Mikrobloggeriet?
Da må du først:

1. Klone ned dette repoet
2. Installere [babashka] og [bbin].
3. Legge til `~/.local/bin` på `PATH`

På mac kan du gjøre noe sånt:

``` shell
brew install borkdude/brew/babashka
brew install babashka/brew/bbin
echo 'export PATH=$PATH:$HOME/.local/bin' >> ~/.zshrc
```

## Installér kommandolinjeprogram for å skrive OLORM-er

Vennligst:

1. Gå til katalogen der du har klonet dette repoet.

2. Installer programmet `olorm`:

    ```shell
    bbin install ./cli --as olorm --main-opts '["-m" "mikrobloggeriet.olorm-cli"]'
    ```

3. Bruk subkommandoen `olorm set-repo-path` til å peke til der du har klonet repoet.
   Kjør `olorm set-repo-path -h` for å se hjelpetekst for subkommadoen.

## Installér kommandolinjeprogram for å skrive JALS-er

Vennligst:

1. Gå til katalogen der du har klonet dette repoet.

2. Installer programmet `jals`:

    ```shell
    bbin install ./cli --as jals --main-opts '["-m" "mikrobloggeriet.jals-cli"]'
    ```

3. Bruk subkommandoen `jals set-repo-path` til å peke til der du har klonet repoet.
   Kjør `jals set-repo-path -h` for å se hjelpetekst for subkommadoen.

[babashka]: https://babashka.org/
[bbin]: https://github.com/babashka/bbin

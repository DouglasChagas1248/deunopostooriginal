# DeuNoPoste Original — Android

Aplicativo Android em Kotlin/XML para consultar resultados, tabela e livro dos sonhos, com cache SQLite e notificações Firebase.

## Versão atual

- Versão do aplicativo: **1.15** (`versionCode = 15`).
- Android mínimo: API 23; compilação e alvo: API 36.
- Gradle Wrapper: 9.8.2; Android Gradle Plugin: 9.5.0.
- JDK do daemon: JetBrains 21, definido em `gradle/gradle-daemon-jvm.properties`.

## Configuração local

1. Abra esta pasta no Android Studio e configure o Android SDK em `local.properties`.
2. Obtenha o arquivo `google-services.json` do projeto Firebase autorizado e coloque-o em `app/`. Esse arquivo é local e não faz parte do repositório.
3. Copie `secrets.properties.example` para `secrets.properties` e configure `DP_API_USER` e `DP_API_PASSWORD`. Também é possível fornecer essas propriedades pelo Gradle ou por variáveis de ambiente com os mesmos nomes.
4. Sincronize o Gradle e compile o módulo `app`.

Credenciais, chaves de assinatura, APKs/AABs e arquivos de IDE/cache ficam fora do Git. A configuração local é incorporada ao APK durante a compilação; o APK continua sendo um cliente que utiliza autenticação Basic.

## Anúncios e layout

A versão 1.15 padroniza Resultados, Sonhos e Significado com toolbar, áreas seguras das barras do Android e um contêiner exclusivo para o banner adaptativo. O espaço integral do anúncio é reservado antes da solicitação e permanece separado do conteúdo rolável. O banner é redimensionado quando a largura disponível muda e acompanha o ciclo de vida da tela.

Compilações de debug usam o bloco de anúncios de teste do Google. Compilações de produção usam o bloco configurado nos recursos do aplicativo.

## Verificação

```powershell
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

Os testes instrumentados verificam que o banner cabe integralmente, não se sobrepõe à toolbar nem ao conteúdo e deixam capturas em `Android/data/br.com.dotchagas.deunoposteorig.deunoposteorig/files/admob-validation` no dispositivo de teste. Verifique também telas pequenas, fontes ampliadas e as orientações suportadas.

A compilação e os testes locais não substituem a publicação de uma versão assinada e a revisão do AdMob. O repositório não publica atualizações automaticamente.

## Integrações

O aplicativo consome a API em `deunoposte-api.dotchagas.com.br`. Os projetos locais `DeuNoPosteServer`, `DeuNoPosteCollector` e `dp` são componentes separados e não estão incluídos neste repositório Android.

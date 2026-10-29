# DeuNoPoste Original — Android

Aplicativo Android em Kotlin/XML para consultar resultados, tabela e livro dos sonhos, com cache SQLite e notificações Firebase.

## Versão atual

- Versão do aplicativo: **1.17** (`versionCode = 17`).
- Android mínimo: API 24 (Android 7.0); compilação e alvo: API 36.
- Gradle Wrapper: 9.8.2; Android Gradle Plugin: 9.5.0.
- JDK do daemon: JetBrains 21, definido em `gradle/gradle-daemon-jvm.properties`.

A versão 1.17 habilita o R8 na compilação de produção para reduzir, otimizar e ofuscar o código, além de remover recursos não utilizados. Isso trata a configuração que produzia o alerta de ofuscação em 0% no Google Play. Os percentuais e a conformidade com os limites de fevereiro de 2027 devem ser confirmados no Play Console após o envio do novo AAB.

A versão 1.16 elevou o SDK mínimo para API 24 para atender ao requisito da Proteção Automática do Google Play.

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
.\gradlew.bat :app:bundleRelease :app:assembleRelease :app:lintRelease
```

Os testes instrumentados verificam que o banner cabe integralmente, não se sobrepõe à toolbar nem ao conteúdo e deixam capturas em `Android/data/br.com.dotchagas.deunoposteorig.deunoposteorig/files/admob-validation` no dispositivo de teste. Verifique também telas pequenas, fontes ampliadas e as orientações suportadas.

Antes de publicar, teste a compilação de produção otimizada, incluindo a seleção das loterias, consulta por data, livro dos sonhos e notificações. Os métodos chamados por `android:onClick` são preservados pelas regras padrão de `proguard-android-optimize.txt`; não há regras gerais que impeçam a ofuscação do aplicativo inteiro. Guarde os arquivos de `app/build/outputs/mapping/release/`, principalmente `mapping.txt`, para interpretar falhas dessa versão. Ao gerar o AAB pelo Android Studio, selecione a variante `release` e a chave de assinatura de produção.

`OptimizationSmokeInstrumentedTest` verifica os seis botões de seleção de loteria chamados pelo XML e a leitura de significado/palpite do cache SQLite. Para validar o R8, execute também esses testes contra uma compilação `release` otimizada assinada apenas para o dispositivo local de testes; não publique o pacote assinado com a chave de debug.

A compilação e os testes locais não substituem a publicação de uma versão assinada e a revisão do AdMob. O repositório não publica atualizações automaticamente.

## Integrações

O aplicativo consome a API em `deunoposte-api.dotchagas.com.br`. Os projetos locais `DeuNoPosteServer`, `DeuNoPosteCollector` e `dp` são componentes separados e não estão incluídos neste repositório Android.

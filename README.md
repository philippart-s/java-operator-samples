# java-operator-samples
Source code with exemples of Kubernetes operators developed with the Java language

## 🎉 Init project
 - la branche `01-init-project` contient le résultat de cette étape
 - [installer / mettre](https://sdk.operatorframework.io/docs/installation/) à jour la dernière version du [Operator SDK](https://sdk.operatorframework.io/) (v1.28 au moment de l'écriture du readme)
 - créer le répertoire `java-operator-samples`
 - dans le répertoire `java-operator-samples `, scaffolding du projet avec Quarkus : `operator-sdk init --plugins quarkus --domain wilda.fr --project-name java-operator-samples`
 - l'arborescence générée est la suivante:
```bash
.
├── Makefile
├── PROJECT
├── README.md
├── pom.xml
├── src
│   └── main
│       ├── java
│       └── resources
│           └── application.properties
```
 - ℹ pour utiliser la dernière version de JOSDK et de l'extension il faut mettre à jour à la main les dépendances (`5.1.1` / `2.16.4.Final` au moment de l'écriture de ce tuto): 
```xml
  <!-- ... -->
  <properties>
    <!-- ... -->
    <quarkus-sdk.version>6.0.1</quarkus-sdk.version>
    <quarkus.version>3.0.0.Final</quarkus.version>
  </properties>
  <!-- ... -->
```
 - vérification que cela compile : `mvn clean compile`
 - tester le lancement: `mvn quarkus:dev`:
```bash
__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
2023-05-01 18:31:58,873 WARN  [io.fab.kub.cli.uti.HttpClientUtils] (Quarkus Main Thread) There are multiple httpclient implementation in the classpath, choosing the first non-default implementation. You should exclude dependencies that aren't needed or use an explicit association of the HttpClient.Factory.

2023-05-01 18:31:59,025 INFO  [io.qua.ope.run.ConfigurationServiceRecorder] (Quarkus Main Thread) Leader election deactivated for dev profile
2023-05-01 18:31:59,082 INFO  [io.qua.ope.run.OperatorProducer] (Quarkus Main Thread) Quarkus Java Operator SDK extension 6.0.1 (commit: b07f98e on branch: b07f98e7fd5877c8f0ac0c18d180229a7ef15104) built on Wed Apr 26 10:46:17 CEST 2023
2023-05-01 18:31:59,084 WARN  [io.qua.ope.run.AppEventListener] (Quarkus Main Thread) No Reconciler implementation was found so the Operator was not started.
2023-05-01 18:31:59,127 INFO  [io.quarkus] (Quarkus Main Thread) java-operator-samples 0.0.1-SNAPSHOT on JVM (powered by Quarkus 3.0.0.Final) started in 2.366s. Listening on: http://localhost:8080
2023-05-01 18:31:59,128 INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
2023-05-01 18:31:59,128 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cdi, kubernetes, kubernetes-client, micrometer, openshift-client, operator-sdk, smallrye-context-propagation, smallrye-health, vertx]
```
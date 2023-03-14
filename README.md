# java-operator-samples
Source code with exemples of Kubernetes operators developed with the Java language

## 🎉 Init project
 - la branche `01-init-project` contient le résultat de cette étape
 - [installer / mettre](https://sdk.operatorframework.io/docs/installation/) à jour la dernière version du [Operator SDK](https://sdk.operatorframework.io/) (v1.27 au moment de l'écriture du readme)
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
 - vérification que cela compile : `mvn clean compile`
 - tester le lancement: `mvn quarkus:dev`:
```bash
__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
2022-11-25 16:25:58,525 INFO  [io.qua.ope.run.OperatorProducer] (Quarkus Main Thread) Quarkus Java Operator SDK extension 4.0.3 (commit: d88d41d on branch: d88d41d78baf198fa4e69d1205f9d19ee04d8c60) built on Thu Oct 06 22:26:39 CEST 2022
2022-11-25 16:25:58,530 WARN  [io.qua.ope.run.AppEventListener] (Quarkus Main Thread) No Reconciler implementation was found so the Operator was not started.
2022-11-25 16:25:58,588 INFO  [io.quarkus] (Quarkus Main Thread) java-operator-samples 0.0.1-SNAPSHOT on JVM (powered by Quarkus 2.13.1.Final) started in 2.250s. Listening on: http://localhost:8080
2022-11-25 16:25:58,589 INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
2022-11-25 16:25:58,589 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cdi, kubernetes, kubernetes-client, micrometer, openshift-client, operator-sdk, smallrye-context-propagation, smallrye-health, vertx]
```
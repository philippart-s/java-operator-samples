# java-operator-samples
Source code with exemples of Kubernetes operators developed with the Java language

## 🎉 Init project
 - la branche `01-init-project` contient le résultat de cette étape
 - [installer / mettre](https://sdk.operatorframework.io/docs/installation/) à jour la dernière version du [Operator SDK](https://sdk.operatorframework.io/) (v1.30 au moment de l'écriture du readme)
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
 - ℹ pour utiliser la dernière version de JOSDK et de l'extension il faut mettre à jour à la main les dépendances (`6.1.1` / `3.1.0.Final` au moment de l'écriture de ce tuto): 
```xml
  <!-- ... -->
  <properties>
    <!-- ... -->
    <quarkus-sdk.version>6.2.1</quarkus-sdk.version>
    <quarkus.version>3.2.0.Final</quarkus.version>
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
2023-07-07 06:22:01,757 INFO  [io.qua.ope.run.ConfigurationServiceRecorder] (Quarkus Main Thread) Leader election deactivated because it is only activated for [prod] profiles. Currently active profiles: [dev]
2023-07-07 06:22:01,951 INFO  [io.qua.ope.run.OperatorProducer] (Quarkus Main Thread) Quarkus Java Operator SDK extension 6.2.1 (commit: 2ba533d on branch: 2ba533dc2c2cf7ab3083a641f7a1badca5d68a62) built on Tue Jul 04 13:00:42 UTC 2023
2023-07-07 06:22:01,952 WARN  [io.qua.ope.run.AppEventListener] (Quarkus Main Thread) No Reconciler implementation was found so the Operator was not started.
2023-07-07 06:22:02,084 INFO  [io.quarkus] (Quarkus Main Thread) java-operator-samples 0.0.1-SNAPSHOT on JVM (powered by Quarkus 3.2.0.Final) started in 7.022s. Listening on: http://localhost:8080
2023-07-07 06:22:02,087 INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
2023-07-07 06:22:02,088 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cdi, kubernetes, kubernetes-client, micrometer, openshift-client, operator-sdk, smallrye-context-propagation, smallrye-health, vertx]
```

## 📄 CRD generation
 - la branche `02-crd-generation` contient le résultat de cette étape
 - création de l'API : `operator-sdk create api --version v1 --kind ReleaseDetector`
 - cette commande a créé les 4 classes nécessaires pour créer l'opérateur:
```bash
src
└── main
    ├── java
    │   └── fr
    │       └── wilda
    │           ├── ReleaseDetector.java
    │           ├── ReleaseDetectorReconciler.java
    │           ├── ReleaseDetectorSpec.java
    │           └── ReleaseDetectorStatus.java
```
  - activer la mise à jour automatique de la CRD:
```properties
quarkus.container-image.build=true
#quarkus.container-image.group=
quarkus.container-image.name=java-operator-samples-operator
# set to true to automatically apply CRDs to the cluster when they get regenerated
quarkus.operator-sdk.crd.apply=true
```
  - tester que tout compile et que la CRD se génère bien: `mvn clean compile` (ou restez en mode `mvn quarkus:dev` pour voir la magie opérer en direct :wink:)
  - ⚠️ Il se peut que vous ayez une erreur de la forme : 
``` bash
2023-03-14 08:31:41,865 ERROR [io.jav.ope.Operator] (Quarkus Main Thread) Error starting operator: io.fabric8.kubernetes.client.KubernetesClientException: Operation: [get]  for kind: [CustomResourceDefinition]  with name: [releasedetectors.wilda.fr]  in namespace: [null]  failed.
...
2023-03-14 08:31:41,880 ERROR [io.qua.run.Application] (Quarkus Main Thread) Failed to start application (with profile dev): java.net.UnknownHostException: kubernetes.default.svc
```
  Dans ce cas vérifiez que vous avez un seul fichier kubeconfig dans votre path (variable d'environnement `KUBECONFIG`)
  - la CRD doit être générée dans le target, `target/kubernetes/releasedetectors.wilda.fr-v1.yml`:
```yaml
# Generated by Fabric8 CRDGenerator, manual edits might get overwritten!
apiVersion: apiextensions.k8s.io/v1
kind: CustomResourceDefinition
metadata:
  name: releasedetectors.wilda.fr
spec:
  group: wilda.fr
  names:
    kind: ReleaseDetector
    plural: releasedetectors
    singular: releasedetector
  scope: Namespaced
  versions:
  - name: v1
    schema:
      openAPIV3Schema:
        properties:
          spec:
            type: object
          status:
            type: object
        type: object
    served: true
    storage: true
    subresources:
      status: {}
```
  - elle doit aussi être installée sur le cluster:
```bash
$ kubectl get crds releasedetectors.wilda.fr
NAME                        CREATED AT

releasedetectors.wilda.fr   2022-08-26T15:40:19Z
```
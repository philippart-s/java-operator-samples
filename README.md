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
 - ℹ pour utiliser la dernière version de JOSDK et de l'extension il faut mettre à jour à la main les dépendances (`6.1.1` / `3.1.0.Final` au moment de l'écriture de ce tuto): 
```xml
  <!-- ... -->
  <properties>
    <!-- ... -->
    <quarkus-sdk.version>6.1.1</quarkus-sdk.version>
    <quarkus.version>3.1.0.Final</quarkus.version>
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
2023-05-01 18:31:59,025 INFO  [io.qua.ope.run.ConfigurationServiceRecorder] (Quarkus Main Thread) Leader election deactivated for dev profile
2023-06-20 15:33:54,931 INFO  [io.qua.ope.run.OperatorProducer] (Quarkus Main Thread) Quarkus Java Operator SDK extension 6.1.1 (commit: 42b0ce2 on branch: 42b0ce29cb78360650c4c409dab43bfe752b5f1a) built on Fri Jun 02 17:11:25 UTC 2023
2023-06-20 15:33:54,932 WARN  [io.qua.ope.run.AppEventListener] (Quarkus Main Thread) No Reconciler implementation was found so the Operator was not started.
2023-06-20 15:33:55,072 INFO  [io.quarkus] (Quarkus Main Thread) java-operator-samples 0.0.1-SNAPSHOT on JVM (powered by Quarkus 3.1.0.Final) started in 7.600s. Listening on: http://localhost:8080
2023-05-01 18:31:59,128 INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
2023-05-01 18:31:59,128 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cdi, kubernetes, kubernetes-client, micrometer, openshift-client, operator-sdk, smallrye-context-propagation, smallrye-health, vertx]
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
  - tester que tout compile que la CRD se génère bien: `mvn clean compile` (ou restez en mode `mvn quarkus:dev` pour voir la magie opérer en direct :wink:)
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

## 👋 Hello,World!
 - la branche `03-hello-world` contient le résultat de cette étape
 - ajouter un champ `name` dans `ReleaseDetectorSpec.java`:
```java
public class ReleaseDetectorSpec {
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
```
  - vérifier que la CRD a bien été mise à jour:
```bash
$ kubectl get crds releasedetectors.wilda.fr -o json | jq '.spec.versions[0].schema.openAPIV3Schema.properties.spec'

{
  "properties": {
    "name": {
      "type": "string"
    }
  },
  "type": "object"
}
```
 - modifier le reconciler `ReleaseDetectorReconciler.java`:
```java
public class ReleaseDetectorReconciler
    implements Reconciler<ReleaseDetector>, Cleaner<ReleaseDetector> {
  private static final Logger log = LoggerFactory.getLogger(ReleaseDetectorReconciler.class);
  private final KubernetesClient client;

  public ReleaseDetectorReconciler(KubernetesClient client) {
    this.client = client;
  }

  @Override
  public UpdateControl<ReleaseDetector> reconcile(ReleaseDetector resource, Context context) {
    log.info("👋 Hello, World 🌏! From {} ", resource.getSpec().getName());

    return UpdateControl.noUpdate();
  }

  @Override
  public DeleteControl cleanup(ReleaseDetector resource, Context<ReleaseDetector> context) {
    log.info("🥲  Goodbye, World 🌏! From {}", resource.getSpec().getName());

    return DeleteControl.defaultDelete();
  }
}    
```
  - créer le namespace `test-hello-world-operator`: `kubectl create ns test-hello-world-operator`
  - créer la CR `src/test/resources/cr-test-hello-world.yaml` pour tester:
```yaml
apiVersion: "wilda.fr/v1"
kind: ReleaseDetector
metadata:
  name: hello-world
spec:
  name: the Moon 🌕!
```
  - créer la CR dans Kubernetes : `kubectl apply -f ./src/test/resources/cr-test-hello-world.yaml -n test-hello-world-operator`
  - la sortie de l'opérateur devrait afficher le message `INFO  [fr.wil.ReleaseDetectorReconciler] (EventHandler-releasedetectorreconciler) 👋 Hello, World 🌏! From the Moon 🌕!`
  - supprimer la CR : `kubectl delete releasedetectors.wilda.fr hello-world -n test-hello-world-operator`
  - la sortie de l'opérateur devrait afficher le message `INFO  [fr.wil.ReleaseDetectorReconciler] (EventHandler-releasedetectorreconciler) 🥲  Goodbye, World 🌏! From the Moon 🌕!`

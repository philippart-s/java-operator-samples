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
  - désactiver, pour l'instant, la création de l'image :
```properties
quarkus.container-image.build=false
#quarkus.container-image.group=
quarkus.container-image.name=java-operator-samples-operator
# set to true to automatically apply CRDs to the cluster when they get regenerated
quarkus.operator-sdk.crd.apply=false
```
  - tester que tout compile que la CRD se génère bien: `mvn clean package` (ou restez en mode `mvn quarkus:dev` pour voir la magie opérer en direct :wink:)
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

## 📝  CRD auto apply
 - la branche `03-auto-apply-crd` contient le résultat de cette étape
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

## 👀  Release detection
 - la branche `04-release-detection` contient le résultat de cette étape
 - ajouter les dépendances suivantes dans le pom.xml:
```xml
  <!-- To call GH API-->
  <dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-rest-client</artifactId>
  </dependency>
  <dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-rest-client-jackson</artifactId>
  </dependency>
``` 
  - créer le POJO `GitHubRelease.java`:
```java
public class GitHubRelease {
  /**
   * ID of the response
   */
  private long responseId;

  /**
   * Release tag name.
   */
  @JsonProperty("tag_name")
  private String tagName;

  public String getTagName() {
    return tagName;
  }

  public void setTagName(String tagName) {
    this.tagName = tagName;
  }
}
```
  - créer le service `GHService.java`:
```java
@Path("/repos")
@RegisterRestClient
public interface GHService {

    @GET
    @Path("/{owner}/{repo}/releases/latest")
    GitHubRelease getByOrgaAndRepo(@PathParam(value = "owner") String owner, @PathParam(value = "repo") String repo);
}
```
 - modifier le fichier `application.properties`:
```properties
quarkus.container-image.build=false
#quarkus.container-image.group=
quarkus.container-image.name=java-operator-samples-operator
# set to true to automatically apply CRDs to the cluster when they get regenerated
quarkus.operator-sdk.crd.apply=false
# set to true to automatically generate CSV from your code
quarkus.operator-sdk.generate-csv=false
# GH Service parameter
quarkus.rest-client."fr.wilda.util.GHService".url=https://api.github.com 
quarkus.rest-client."fr.wilda.util.GHService".scope=javax.inject.Singleton 
```
 - modifier la partie _spec_ de la CRD en modifiant la classe `ReleaseDetectorSpec`:
```java
public class ReleaseDetectorSpec {
    /**
     * Name of the organisation (or owner) where find the repository
     */
    private String organisation;
    /**
     * The repository name.
     */
    private String repository;

    
    public String getOrganisation() {
        return organisation;
    }
    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }
    public String getRepository() {
        return repository;
    }
    public void setRepository(String repository) {
        this.repository = repository;
    }
}
```
 - modifier la partie _status_ de la CRD en modifiant la classe `ReleaseDetectorStatus`:
```java
public class ReleaseDetectorStatus {

    /**
     * Last release version deployed on the cluster.
     */
    private String deployedRelase;

    public String getDeployedRelase() {
        return deployedRelase;
    }

    public void setDeployedRelase(String deployedRelase) {
        this.deployedRelase = deployedRelase;
    }
}
```
  - update the reconciler `ReleaseDetectorReconciler.java`:
```java
public class ReleaseDetectorReconciler implements Reconciler<ReleaseDetector>,
    Cleaner<ReleaseDetector>, EventSourceInitializer<ReleaseDetector> {
  private static final Logger log = LoggerFactory.getLogger(ReleaseDetectorReconciler.class);

  /**
   * Name of the repository to check.
   */
  private String repoName;
  /**
   * GitHub organisation name that contains the repository.
   */
  private String organisationName;
  /**
   * ID of the created custom resource.
   */
  private ResourceID resourceID;
  /**
   * Current deployed release.
   */
  private String currentRelease;
  /**
   * Fabric0 kubernetes client.
   */
  private final KubernetesClient client;

  @Inject
  @RestClient
  private GHService ghService;

  public ReleaseDetectorReconciler(KubernetesClient client) {
    this.client = client;
  }

  @Override
  public Map<String, EventSource> prepareEventSources(EventSourceContext<ReleaseDetector> context) {
    var poolingEventSource = new PollingEventSource<String, ReleaseDetector>(() -> {
      log.info("⚡️ Polling data !");
      if (resourceID != null) {
        log.info("🚀 Fetch resources !");
        log.info("🐙 Get the last release version of repository {} in organisation {}.",
            organisationName, repoName);
        GitHubRelease gitHubRelease = ghService.getByOrgaAndRepo(organisationName, repoName);
        log.info("🏷  Last release is {}", gitHubRelease.getTagName());
        currentRelease = gitHubRelease.getTagName();
        return Map.of(resourceID, Set.of(currentRelease));
      } else {
        log.info("🚫 No resource created, nothing to do.");
        return Map.of();
      }
    }, 30000, String.class);

    return EventSourceInitializer.nameEventSources(poolingEventSource);
  }

  @Override
  public UpdateControl<ReleaseDetector> reconcile(ReleaseDetector resource, Context context) {
    log.info("⚡️ Event occurs ! Reconcile called.");

    // Get configuration
    resourceID = ResourceID.fromResource(resource);
    repoName = resource.getSpec().getRepository();
    organisationName = resource.getSpec().getOrganisation();
    log.info("⚙️ Configuration values : repository = {}, organisation = {}.", repoName,
        organisationName);

    // Update the status
    if (resource.getStatus() != null) {
      resource.getStatus().setDeployedRelase(currentRelease);
    } else {
      resource.setStatus(new ReleaseDetectorStatus());
    }

    return UpdateControl.patchStatus(resource);
  }

  @Override
  public DeleteControl cleanup(ReleaseDetector resource, Context<ReleaseDetector> context) {
    log.info("🗑 Undeploy the application");

    resourceID = null;

    return DeleteControl.defaultDelete();
  }
}
```
  - créer la CR de tests `cr-test-gh-release-watch.yml`:
```yaml
apiVersion: "fr.wilda/v1"
kind: ReleaseDetector
metadata:
  name: check-quarkus
spec:
  organisation: philippart-s
  repository: hello-world-from-quarkus
```
  - une fois Quarkus rechargé la sortie des logs devrait être de la forme:
```bash
INFO  [fr.wil.ReleaseDetectorReconciler] (Timer-4) ⚡️ Polling data !
INFO  [fr.wil.ReleaseDetectorReconciler] (Timer-4) 🚫 No resource created, nothing to do.
INFO  [fr.wil.ReleaseDetectorReconciler] (Timer-4) ⚡️ Polling data !
INFO  [fr.wil.ReleaseDetectorReconciler] (Timer-4) 🚫 No resource created, nothing to do.
INFO  [fr.wil.ReleaseDetectorReconciler] (Timer-4) ⚡️ Polling data !
INFO  [fr.wil.ReleaseDetectorReconciler] (Timer-4) 🚫 No resource created, nothing to do.
```
  - créer le namespace de test : `kubectl create ns test-java-operator-samples`
  - créer la custom resource de tests `src/test/resources/cr-test-gh-release-watch.yml`:
```yaml
apiVersion: "wilda.fr/v1"
kind: ReleaseDetector
metadata:
  name: check-quarkus
spec:
  organisation: philippart-s
  repository: hello-world-from-quarkus
``` 
  - puis la créer sur le cluster: `kubectl apply -f ./src/test/resources/cr-test-gh-release-watch.yml -n test-java-operator-samples`
  - les logs devraient être de la forme:
```bash
INFO  [fr.wil.ReleaseDetectorReconciler] (EventHandler-releasedetectorreconciler) ⚡️ Event occurs ! Reconcile called.
INFO  [fr.wil.ReleaseDetectorReconciler] (EventHandler-releasedetectorreconciler) ⚙️ Configuration values : repository = hello-world-from-quarkus, organisation = philippart-s.
INFO  [fr.wil.ReleaseDetectorReconciler] (Timer-6) ⚡️ Polling data !
INFO  [fr.wil.ReleaseDetectorReconciler] (Timer-6) 🚀 Fetch resources !
INFO  [fr.wil.ReleaseDetectorReconciler] (Timer-6) 🐙 Get the last release version of repository philippart-s in organisation hello-world-from-quarkus.
INFO  [fr.wil.ReleaseDetectorReconciler] (Timer-6) 🏷  Last release is 1.0.4
```
  - supprimer la CR créée : `kubectl delete releasedetectors.wilda.fr check-quarkus -n test-java-operator-samples`

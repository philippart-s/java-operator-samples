# java-operator-samples
Source code with exemples of Kubernetes operators developed with the Java language

## 🎉 Init project
 - la branche `01-init-project` contient le résultat de cette étape
 - [installer / mettre](https://sdk.operatorframework.io/docs/installation/) à jour la dernière version du [Operator SDK](https://sdk.operatorframework.io/) (v1.22.2 au moment de l'écriture du readme)
 - créer le répertoire `java-operator-samples `
 - dans le répertoire `java-operator-samples `, scaffolding du projet avec Quarkus : `operator-sdk init --plugins quarkus --domain wilda.fr --project-name java-operator-samples`
 - l'arborescence générée est la suivante:
```bash
.
├── LICENSE
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
 - ⚠️ Au moment de l'écriture de ce tuto il est nécessaire de changer manuellement les versions de Quarkus et du SDK dans le `pom.xml`:
    - passer la propriété `quarkus.version` à `2.11.2.Final`
    - passer la propriété `quarkus-sdk.version` à `4.0.0`
 - Supprimer la dépendance `quarkus-operator-sdk-csv-generator` qui n'est plus dans le bom
 - vérification que cela compile : `mvn clean compile`
 - tester le lancement: `mvn quarkus:dev`:
```bash
__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
2022-08-26 17:36:38,732 WARN  [io.qua.config] (Quarkus Main Thread) Unrecognized configuration key "quarkus.operator-sdk.generate-csv" was provided; it will be ignored; verify that the dependency extension for this configuration is set or that you did not make a typo

2022-08-26 17:36:38,968 WARN  [io.fab.kub.cli.Config] (Quarkus Main Thread) Found multiple Kubernetes config files [[/Users/sphilipp/dev/ovh/k8s/kubeconfig-example2.yml, /Users/sphilipp/dev/ovh/k8s/big-k8s.yml]], using the first one: [/Users/sphilipp/dev/ovh/k8s/kubeconfig-example2.yml]. If not desired file, please change it by doing `export KUBECONFIG=/path/to/kubeconfig` on Unix systems or `$Env:KUBECONFIG=/path/to/kubeconfig` on Windows.
2022-08-26 17:36:39,041 WARN  [io.fab.kub.cli.Config] (Quarkus Main Thread) Found multiple Kubernetes config files [[/Users/sphilipp/dev/ovh/k8s/kubeconfig-example2.yml, /Users/sphilipp/dev/ovh/k8s/big-k8s.yml]], using the first one: [/Users/sphilipp/dev/ovh/k8s/kubeconfig-example2.yml]. If not desired file, please change it by doing `export KUBECONFIG=/path/to/kubeconfig` on Unix systems or `$Env:KUBECONFIG=/path/to/kubeconfig` on Windows.
2022-08-26 17:36:39,160 INFO  [io.qua.ope.run.OperatorProducer] (Quarkus Main Thread) Quarkus Java Operator SDK extension 4.0.0 (commit: abfa719 on branch: abfa71954a61b9fab0c7561bff68d85c3037f369) built on Sun Aug 14 22:20:17 CEST 2022
2022-08-26 17:36:39,162 WARN  [io.qua.ope.run.AppEventListener] (Quarkus Main Thread) No Reconciler implementation was found so the Operator was not started.
2022-08-26 17:36:39,216 INFO  [io.quarkus] (Quarkus Main Thread) java-operator-samples 0.0.1-SNAPSHOT on JVM (powered by Quarkus 2.11.2.Final) started in 2.257s. Listening on: http://localhost:8080
2022-08-26 17:36:39,217 INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
2022-08-26 17:36:39,217 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cdi, kubernetes, kubernetes-client, micrometer, openshift-client, operator-sdk, smallrye-context-propagation, smallrye-health, vertx]
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
  - tester que tout compile que la CRD se génère bien: `mvn clean package` (ou restez en mode `mvn quarkus:dev` pour voir la magie opérer en direct :wink:)
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
    log.info("👋 Hello, World 🌏 ! From {} ", resource.getSpec().getName());

    return UpdateControl.noUpdate();
  }

  @Override
  public DeleteControl cleanup(ReleaseDetector resource, Context<ReleaseDetector> context) {
    log.info("🥲  Goodbye, World 🌏 ! From {}", resource.getSpec().getName());

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
  name: the Moon 🌕 !
```
  - créer la CR dans Kubernetes : `kubectl apply -f ./src/test/resources/cr-test-hello-world.yaml -n test-hello-world-operator`
  - la sortie de l'opérateur devrait afficher le message `INFO  [fr.wil.ReleaseDetectorReconciler] (EventHandler-releasedetectorreconciler) 👋 Hello, World 🌏 ! From the Moon 🌕 ! `
  - supprimer la CR : `kubectl delete releasedetectors.wilda.fr hello-world -n test-hello-world-operator`
  - la sortie de l'opérateur devrait afficher le message `INFO  [fr.wil.ReleaseDetectorReconciler] (EventHandler-releasedetectorreconciler) 🥲  Goodbye, World 🌏 ! From the Moon 🌕 !`

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
quarkus.container-image.build=true
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
INFO  [fr.wil.ReleaseDetectorReconciler] (Timer-6) 🏷  Last release is 1.0.0
```
  - vérifier la mise à jour du status de la CR : `kubectl get ReleaseDetector/check-quarkus -n test-java-operator-samples -o yaml`
  - supprimer la CR créée : `kubectl delete releasedetectors.wilda.fr check-quarkus -n test-java-operator-samples`

## 🔀 Deploy application
  - la branche `05-deploy-app` contient le résultat de cette étape
  - modifier le reconciler `ReleaseDetectorReconciler`:
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

    String namespace = resource.getMetadata().getNamespace();

    // Get configuration
    resourceID = ResourceID.fromResource(resource);
    repoName = resource.getSpec().getRepository();
    organisationName = resource.getSpec().getOrganisation();
    log.info("⚙️ Configuration values : repository = {}, organisation = {}.", repoName,
        organisationName);

    if (currentRelease != null && currentRelease.trim().length() != 0) {
      // Deploy appllication
      log.info("🔀 Deploy the new release {} !", currentRelease);
      Deployment deployment = makeDeployment(currentRelease, resource);
      client.apps().deployments().inNamespace(namespace).createOrReplace(deployment);

      // Create service
      Service service = makeService(resource);
      Service existingService = client.services().inNamespace(resource.getMetadata().getNamespace())
          .withName(service.getMetadata().getName()).get();
      if (existingService == null) {
        client.services().inNamespace(namespace).createOrReplace(service);
      }

      // Update the status
      if (resource.getStatus() != null) {
        resource.getStatus().setDeployedRelase(currentRelease);
      } else {
        resource.setStatus(new ReleaseDetectorStatus());
      }
    }

    return UpdateControl.noUpdate();
  }

  @Override
  public DeleteControl cleanup(ReleaseDetector resource, Context<ReleaseDetector> context) {
    log.info("🗑 Undeploy the application");

    resourceID = null;

    return DeleteControl.defaultDelete();
  }

  /**
   * Generate the Kubernetes deployment resource.
   * @param currentRelease The release to deploy
   * @param releaseDetector The created custom resource
   * @return The created deployment
   */
  private Deployment makeDeployment(String currentRelease, ReleaseDetector releaseDetector) {
    Deployment deployment = new DeploymentBuilder()
    .withNewMetadata()
      .withName("quarkus-deployment")
      .addToLabels("app", "quarkus")
      .endMetadata()
    .withNewSpec()
      .withReplicas(1)
      .withNewSelector()
        .withMatchLabels(Map.of("app", "quarkus"))
      .endSelector()
      .withNewTemplate()
        .withNewMetadata()
          .addToLabels("app","quarkus")
        .endMetadata()
        .withNewSpec()
          .addNewContainer()
            .withName("quarkus")
            .withImage("wilda/" + repoName + ":" + currentRelease)
            .addNewPort()
              .withContainerPort(80)
            .endPort()
          .endContainer()
        .endSpec()
      .endTemplate()
    .endSpec()
    .build();

    deployment.addOwnerReference(releaseDetector);

    try {
      log.info("Generated deployment {}", SerializationUtils.dumpAsYaml(deployment));
    } catch (JsonProcessingException e) {
      log.error("Unable to get YML");
      e.printStackTrace();
    }

    return deployment;
  }

  /**
   * Generate the Kubernetes service resource.
   * 
   * @param releaseDetector The custom resource
   * @return The service.
   */
  private Service makeService(ReleaseDetector releaseDetector) {
    Service service = new ServiceBuilder()
    .withNewMetadata()
      .withName("quarkus-service")
      .addToLabels("app", "quarkus")
    .endMetadata()
    .withNewSpec()
      .withType("NodePort")
      .withSelector(Map.of("app", "quarkus"))
      .addNewPort()
        .withPort(80)
        .withTargetPort(new IntOrString(8080))
        .withNodePort(30080)
      .endPort()
    .endSpec()
    .build();
    
    service.addOwnerReference(releaseDetector);

    try {
      log.info("Generated service {}", SerializationUtils.dumpAsYaml(service));
    } catch (JsonProcessingException e) {
      log.error("Unable to get YML");
      e.printStackTrace();
    }

    return service;
  }  
}
```
  - recréer la CR : `kubectl apply -f ./src/test/resources/cr-test-gh-release-watch.yml -n test-java-operator-samples`
  - la sortie de l'opérateur devrait être:
```bash
INFO  [fr.wil.ReleaseDetectorReconciler] (Timer-8) 🚀 Fetch resources !
INFO  [fr.wil.ReleaseDetectorReconciler] (Timer-8) 🐙 Get the last release version of repository philippart-s in organisation hello-world-from-quarkus.
INFO  [fr.wil.ReleaseDetectorReconciler] (Timer-8) 🏷  Last release is 1.0.4
INFO  [fr.wil.ReleaseDetectorReconciler] (EventHandler-releasedetectorreconciler) ⚡️ Event occurs ! Reconcile called.
INFO  [fr.wil.ReleaseDetectorReconciler] (EventHandler-releasedetectorreconciler) ⚙️ Configuration values : repository = hello-world-from-quarkus, organisation = philippart-s.
INFO  [fr.wil.ReleaseDetectorReconciler] (EventHandler-releasedetectorreconciler) 🔀 Deploy the new release 1.0.4 !
INFO  [fr.wil.ReleaseDetectorReconciler] (EventHandler-releasedetectorreconciler) Generated deployment ---
apiVersion: "apps/v1"
kind: "Deployment"
metadata:
  labels:
    app: "quarkus"
  name: "quarkus-deployment"
  ownerReferences:
  - apiVersion: "wilda.fr/v1"
    kind: "ReleaseDetector"
    name: "check-quarkus"
    uid: "eadae4da-55a8-4d2b-b989-7a9282231200"
spec:
  replicas: 1
  selector:
    matchLabels:
      app: "quarkus"
  template:
    metadata:
      labels:
        app: "quarkus"
    spec:
      containers:
      - image: "wilda/hello-world-from-quarkus:1.0.4"
        name: "quarkus"
        ports:
        - containerPort: 80


2022-08-26 18:27:38,151 INFO  [fr.wil.ReleaseDetectorReconciler] (EventHandler-releasedetectorreconciler) Generated service ---
apiVersion: "v1"
kind: "Service"
metadata:
  labels:
    app: "quarkus"
  name: "quarkus-service"
  ownerReferences:
  - apiVersion: "wilda.fr/v1"
    kind: "ReleaseDetector"
    name: "check-quarkus"
    uid: "eadae4da-55a8-4d2b-b989-7a9282231200"
spec:
  ports:
  - nodePort: 30080
    port: 80
    targetPort: 8080
  selector:
    app: "quarkus"
  type: "NodePort"


INFO  [fr.wil.ReleaseDetectorReconciler] (Timer-8) ⚡️ Polling data !
INFO  [fr.wil.ReleaseDetectorReconciler] (Timer-8) 🚀 Fetch resources !
INFO  [fr.wil.ReleaseDetectorReconciler] (Timer-8) 🐙 Get the last release version of repository philippart-s in organisation hello-world-from-quarkus.
INFO  [fr.wil.ReleaseDetectorReconciler] (Timer-8) 🏷  Last release is 1.0.4
```
  - vérifier que l'application a été déployée:
```bash
$ kubectl get pods,svc -n test-java-operator-samples

NAME                                      READY   STATUS    RESTARTS   AGE
pod/quarkus-deployment-7b74f6b6ff-2rffc   1/1     Running   0          98s

NAME                      TYPE       CLUSTER-IP    EXTERNAL-IP   PORT(S)        AGE
service/quarkus-service   NodePort   X.X.X.X   <none>        80:30080/TCP   3m8s
```
  - tester l'application :
```bash
$ curl http://http://xxxx.nodes.c1.xxx.k8s.ovh.net:30080/hello

👋  Hello, World ! 🌍
```
  - supprimer la CR: `kubectl delete releasedetectors.wilda.fr check-quarkus -n test-java-operator-samples`
  - vérifier que tout a été supprimé:
```bash
$ kubectl get pods,svc -n test-java-operator-samples

No resources found in test-hello-world-operator namespace.
```
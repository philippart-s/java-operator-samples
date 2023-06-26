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


## ⬆️ Deploy on event

  - la branche `04-deploy-on-event` contient le résultat de cette étape
  - ajouter la dépendance `quarkus-resteasy-reactive-jackson` dans le fichier pom.xml : 
```xml
    <!-- To expose Webhook -->
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-resteasy-reactive-jackson</artifactId>
    </dependency>      
```
  - modifier le reconciler pour utiliser `SimpleInboundEventSource` et déployer l'application si nécessaire :
```java
public class ReleaseDetectorReconciler implements Reconciler<ReleaseDetector>,
    Cleaner<ReleaseDetector>, EventSourceInitializer<ReleaseDetector> {
  private static final Logger log = LoggerFactory.getLogger(ReleaseDetectorReconciler.class);


  /**
   * Flag to know if the operator must deploy the application on a new event. 
   */
  private String deploy = "❌";
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

  private SimpleInboundEventSource simpleInboundEventSource;

  public ReleaseDetectorReconciler(KubernetesClient client) {
    this.client = client;
  }

  @Override
  public Map<String, EventSource> prepareEventSources(EventSourceContext<ReleaseDetector> context) {
    simpleInboundEventSource = createSimpleInboundEventSource();
    return EventSourceInitializer.nameEventSources(simpleInboundEventSource);
  }

  @Override
  public UpdateControl<ReleaseDetector> reconcile(ReleaseDetector resource, Context context) {
    log.info("⚡️ Event occurs ! Reconcile called.");
    
    String namespace = resource.getMetadata().getNamespace();
    String statusDeployedRelease = (resource.getStatus() != null ? resource.getStatus().getDeployedRelase() : "");

    deploy = resource.getSpec().getDeploy();
    log.info("The Quarkus application will be deployed if needed: {}", deploy);

    // Get configuration
    resourceID = ResourceID.fromResource(resource);

    if ("✅".equalsIgnoreCase(deploy) && currentRelease != null && currentRelease.trim().length() != 0 && !currentRelease.equalsIgnoreCase(statusDeployedRelease)) {      
      // Deploy application
      log.info("🔀 Deploy the new release {} !", currentRelease);
      Deployment deployment = makeDeployment(currentRelease, resource);
      Deployment existingDeployment = client.apps().deployments().inNamespace(namespace).withName(deployment.getMetadata().getName()).get();
      if (existingDeployment == null) {
        client.apps().deployments().inNamespace(namespace).resource(deployment).create();
      }

      // Create service
      Service service = makeService(resource);
      Service existingService = client.services().inNamespace(resource.getMetadata().getNamespace())
          .withName(service.getMetadata().getName()).get();
      if (existingService == null) {
        client.services().inNamespace(namespace).resource(service).create();
      }

      // Update the status
      if (resource.getStatus() != null) {
        resource.getStatus().setDeployedRelase(currentRelease);
      } else {
        ReleaseDetectorStatus releaseDetectorStatus = new ReleaseDetectorStatus();
        releaseDetectorStatus.setDeployedRelase(currentRelease);
        resource.setStatus(releaseDetectorStatus);
      }
    }

    return UpdateControl.patchStatus(resource);
  }

  @Override
  public DeleteControl cleanup(ReleaseDetector resource, Context<ReleaseDetector> context) {
    log.info("🗑 Undeploy the application");

    resourceID = null;

    return DeleteControl.defaultDelete();
  }

  /**
   * Generate the Kubernetes deployment resource.
   * 
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
          .addToLabels("app", "quarkus")
        .endMetadata()
        .withNewSpec()
          .addNewContainer()
            .withName("quarkus")
            .withImage("wilda/hello-world-from-quarkus" + ":" + currentRelease)
            .addNewPort()
              .withContainerPort(80)
            .endPort()
          .endContainer()
        .endSpec()
      .endTemplate()
    .endSpec()
    .build();

    deployment.addOwnerReference(releaseDetector);

    log.info("Generated deployment {}", Serialization.asYaml(deployment));

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

    log.info("Generated service {}", Serialization.asYaml(service));

    return service;
  }

    /**
   * Fire an event to awake the reconciler.
   * @param tag The new tag on GitHub.
   */
  public void fireEvent(String tag) {
    if (resourceID != null) {
      currentRelease = tag;
      simpleInboundEventSource.propagateEvent(resourceID);
    } else {
      log.info("🚫 No resource created, nothing to do.");
    }
  }

  @Produces
  public SimpleInboundEventSource createSimpleInboundEventSource() {
    return new SimpleInboundEventSource();
  }
}
```
  - créer le POJO du body du end-point pour le Webhook : 
```java
package fr.wilda.rs;

import java.io.Serializable;

public class GHTagEvent implements Serializable {
  private String ref;
  private String ref_type;
  
  public String getRef() {
    return ref;
  }
  public void setRef(String ref) {
    this.ref = ref;
  }
  public String getRef_type() {
    return ref_type;
  }
  public void setRef_type(String ref_type) {
    this.ref_type = ref_type;
  }
}
```
  - créer le end-point du Webhook : 
```java
@Path("/webhook")
public class WebHookOperator {
  private static final Logger log = LoggerFactory.getLogger(WebHookOperator.class);

  @Inject
  private ReleaseDetectorReconciler detectorReconciler;

  @POST
  @Path("/event")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response newTag(GHTagEvent tagEvent) {
    log.info("⚓️ Webhook called!!!!");

    if ("tag".equalsIgnoreCase(tagEvent.getRef_type())) {
      log.info("🎉 New tag: {}", tagEvent.getRef());
    }

    detectorReconciler.fireEvent(tagEvent.getRef());
    return Response.ok().build();
  }
}
```
  - créer le namespace `test-java-operator-samples` : `kubectl create ns test-java-operator-samples`
  - créer la CR de tests `src/main/test/resources/cr-test-gh-release-watch.yml`:
```yaml
apiVersion: "wilda.fr/v1"
kind: ReleaseDetector
metadata:
  name: check-quarkus
spec:
  deploy: ✅
```
  - appliquer la CR de test : `kubectl apply  -f ./src/test/resources/cr-test-gh-release-watch.yml -n test-java-operator-samples`
```bash
INFO  [fr.wil.ReleaseDetectorReconciler] (ReconcilerExecutor-releasedetectorreconciler-136) ⚡️ Event occurs ! Reconcile called.
INFO  [fr.wil.ReleaseDetectorReconciler] (ReconcilerExecutor-releasedetectorreconciler-136) The Quarkus application will be deployed if needed: ✅
```
  - tester le déclenchement via Webhook : `curl --json '{"ref": "1.0.4", "ref_type": "tag"}' http://localhost:8080/webhook/event`
```bash
2023-04-25 21:24:28,576 INFO  [fr.wil.rs.WebHookOperator] (executor-thread-0) ⚓️ Webhook called!!!!
2023-04-25 21:24:28,577 INFO  [fr.wil.rs.WebHookOperator] (executor-thread-0) 🎉 New tag: 1.0.4
2023-04-25 21:24:28,578 INFO  [fr.wil.ReleaseDetectorReconciler] (ReconcilerExecutor-releasedetectorreconciler-147) ⚡️ Event occurs ! Reconcile called.
2023-04-25 21:24:28,579 INFO  [fr.wil.ReleaseDetectorReconciler] (ReconcilerExecutor-releasedetectorreconciler-147) ⚙️ Configuration values : repository = hello-world-from-quarkus, organisation = philippart-s.
2023-04-25 21:24:28,579 INFO  [fr.wil.ReleaseDetectorReconciler] (ReconcilerExecutor-releasedetectorreconciler-147) 🔀 Deploy the new release 1.0.4 !
2023-04-25 21:24:28,609 INFO  [fr.wil.ReleaseDetectorReconciler] (ReconcilerExecutor-releasedetectorreconciler-147) Generated deployment ---
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
    uid: "d0cfcb5c-1169-4fa0-a246-9901e680a829"
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


2023-04-25 21:24:28,736 INFO  [fr.wil.ReleaseDetectorReconciler] (ReconcilerExecutor-releasedetectorreconciler-147) Generated service ---
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
    uid: "d0cfcb5c-1169-4fa0-a246-9901e680a829"
spec:
  ports:
  - nodePort: 30080
    port: 80
    targetPort: 8080
  selector:
    app: "quarkus"
  type: "NodePort"
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
$ curl http://xxxx.nodes.c1.xxx.k8s.ovh.net:30080/hello

👋  Hello, World ! 🌍
```
  - vérifier le status de la CR: `kubectl get ReleaseDetector/check-quarkus -n test-java-operator-samples -o json | jq '.status'`
```bash
$ kubectl get ReleaseDetector/check-quarkus -n test-java-operator-samples -o json | jq '.status'

{
  "deployedRelase": "1.0.4"
}
```
  - supprimer la CR: `kubectl delete releasedetectors.wilda.fr check-quarkus -n test-java-operator-samples`
  - vérifier que tout a été supprimé:
```bash
$ kubectl get pods,svc -n test-java-operator-samples

No resources found in test-java-operator-samples namespace.
```

## 🐳  Packaging & deployment to K8s
  - la branche `05-deploy-operator` contient le résultat de cette étape
  - arrêter le mode dev de Quarkus
  - modifier le fichier `application.properties`:
```properties
# Image options
quarkus.container-image.build=true
quarkus.container-image.group=wilda
quarkus.container-image.name=java-operator-samples-operator
# Kubernetes options
quarkus.kubernetes.namespace=java-operator-samples-operator
quarkus.kubernetes.namespace=java-operator-samples-operator
```
  - ajouter la dépendance suivante pour générer l'image avec JIB:
```xml
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-container-image-jib</artifactId>
    </dependency>
```
  - ajouter un fichier `src/main/kubernetes/kubernetes.yml` pour créer l'ingress :
```yaml
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  annotations:
    kubernetes.io/ingress.class: nginx
  name: ingress
  namespace: java-operator-samples-operator
spec:
  rules:
  - http:
      paths:
      - backend:
          service:
            name: java-operator-samples-operator
            port:
              number: 80
        path: /
        pathType: Prefix
```
  - lancer le packaging : `mvn clean package`
  - vérifier que l'image a bien été générée: : `docker images | grep java-operator-samples-operator`:
```bash
wilda/java-operator-samples-operator                 0.0.1-SNAPSHOT         cffe16ca153c   54 seconds ago   417MB
```
  - push de l'image : `docker login` && `docker push wilda/java-operator-samples-operator:0.0.1-SNAPSHOT`
  - si nécessaire, créer le namespace `test-java-operator-samples`: `kubectl create ns test-java-operator-samples`
  - si nécessaire, créer le namespace `java-operator-samples-operator`: `kubectl create ns java-operator-samples-operator`
  - si nécessaire créer la CRD: `kubectl apply -f ./target/kubernetes/releasedetectors.wilda.fr-v1.yml`
  - appliquer le manifest créé : `kubectl apply -f ./target/kubernetes/kubernetes.yml`
  - vérifier que tout va bien:
```bash
$ kubectl get pod -n java-operator-samples-operator

NAME                                             READY   STATUS    RESTARTS   AGE
java-operator-samples-operator-8b9cf6766-q6mns   1/1     Running   0          42s   
```
```bash
$ kubectl logs java-operator-samples-operator-8b9cf6766-q6mns -n java-operator-samples-operator

__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
2023-05-01 19:54:05,660 WARN  [io.qua.config] (main) Unrecognized configuration key "quarkus.rest-client."fr.wilda.util.GHService".scope" was provided; it will be ignored; verify that the dependency extension for this configuration is set or that you did not make a typo
2023-05-01 19:54:05,661 WARN  [io.qua.config] (main) Unrecognized configuration key "quarkus.rest-client."fr.wilda.util.GHService".url" was provided; it will be ignored; verify that the dependency extension for this configuration is set or that you did not make a typo
2023-05-01 19:54:07,966 INFO  [io.qua.ope.run.OperatorProducer] (main) Quarkus Java Operator SDK extension 6.0.1 (commit: b07f98e on branch: b07f98e7fd5877c8f0ac0c18d180229a7ef15104) built on Wed Apr 26 08:46:17 GMT 2023
2023-05-01 19:54:08,073 INFO  [io.jav.ope.Operator] (main) Registered reconciler: 'releasedetectorreconciler' for resource: 'class fr.wilda.ReleaseDetector' for namespace(s): [all namespaces]
2023-05-01 19:54:08,073 INFO  [io.qua.ope.run.AppEventListener] (main) Starting operator.
2023-05-01 19:54:08,080 INFO  [io.jav.ope.Operator] (main) Operator SDK 4.3.0 (commit: b410c65) built on Fri Mar 31 08:11:47 GMT 2023 starting...
2023-05-01 19:54:08,081 INFO  [io.jav.ope.Operator] (main) Client version: 6.5.1
2023-05-01 19:54:08,084 INFO  [io.jav.ope.pro.Controller] (Controller Starter for: releasedetectorreconciler) Starting 'releasedetectorreconciler' controller for reconciler: fr.wilda.ReleaseDetectorReconciler, resource: fr.wilda.ReleaseDetector
2023-05-01 19:54:09,290 INFO  [io.jav.ope.pro.Controller] (Controller Starter for: releasedetectorreconciler) 'releasedetectorreconciler' controller started
2023-05-01 19:54:09,341 INFO  [io.quarkus] (main) java-operator-samples 0.0.1-SNAPSHOT on JVM (powered by Quarkus 3.0.0.Final) started in 4.526s. Listening on: http://0.0.0.0:8080
2023-05-01 19:54:09,342 INFO  [io.quarkus] (main) Profile prod activated. 
2023-05-01 19:54:09,342 INFO  [io.quarkus] (main) Installed features: [cdi, kubernetes, kubernetes-client, micrometer, openshift-client, operator-sdk, resteasy-reactive, resteasy-reactive-jackson, smallrye-context-propagation, smallrye-health, vertx]
```
  - créer la CR de test : `kubectl apply -f ./src/test/resources/cr-test-gh-release-watch.yml -n test-java-operator-samples`
  - récupérer l'IP de l'ingress `kubectl get ingress -n java-operator-samples-operator` :
```bash
$ kubectl get ingress -n java-operator-samples-operator
NAME      CLASS    HOSTS   ADDRESS           PORTS   AGE
ingress   <none>   *       XXX.XXX.XXX.XXX   80      2m42s
```
  - tester le déclenchement via Webhook : `curl --json '{"ref": "1.0.4", "ref_type": "tag"}' http://<ingress ip>/webhook/event`
  - constater que l'opérateur n'arrive pas à créer l'application dans le namespace:
```bash
Caused by: io.fabric8.kubernetes.client.KubernetesClientException: Failure executing: POST at: https://10.3.0.1/apis/apps/v1/namespaces/test-jav │
│ a-operator-samples/deployments. Message: Forbidden!Configured service account doesn't have access. Service account may have been revoked. deploy │
│ ments.apps is forbidden: User "system:serviceaccount:java-operator-samples-operator:java-operator-samples-operator" cannot create resource "depl │
│ oyments" in API group "apps" in the namespace "test-java-operator-samples".       
```
  - supprimer la CR: `kubectl delete releasedetectors.wilda.fr check-quarkus -n test-java-operator-samples`

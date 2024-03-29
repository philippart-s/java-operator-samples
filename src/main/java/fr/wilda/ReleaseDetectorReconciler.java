package fr.wilda;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.javaoperatorsdk.operator.api.reconciler.Cleaner;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.EventSourceContext;
import io.javaoperatorsdk.operator.api.reconciler.EventSourceInitializer;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.processing.event.ResourceID;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import io.javaoperatorsdk.operator.processing.event.source.inbound.SimpleInboundEventSource;
import jakarta.ws.rs.Produces;

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
      } else {
        client.apps().deployments().inNamespace(namespace).resource(deployment).update();
      }

      // Create service
      Service service = makeService(resource);
      Service existingService = client.services().inNamespace(resource.getMetadata().getNamespace())
          .withName(service.getMetadata().getName()).get();
      if (existingService == null) {
        client.services().inNamespace(namespace).resource(service).create();
      } else {
        client.services().inNamespace(namespace).resource(service).update();
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

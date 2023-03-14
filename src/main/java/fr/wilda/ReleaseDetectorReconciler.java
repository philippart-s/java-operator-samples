package fr.wilda;

import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import fr.wilda.util.GHService;
import fr.wilda.util.GitHubRelease;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import io.javaoperatorsdk.operator.api.reconciler.Cleaner;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.EventSourceContext;
import io.javaoperatorsdk.operator.api.reconciler.EventSourceInitializer;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.processing.event.ResourceID;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import io.javaoperatorsdk.operator.processing.event.source.polling.PollingEventSource;

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
    Deployment deployment = new DeploymentBuilder().withNewMetadata().withName("quarkus-deployment")
        .addToLabels("app", "quarkus").endMetadata().withNewSpec().withReplicas(1).withNewSelector()
        .withMatchLabels(Map.of("app", "quarkus")).endSelector().withNewTemplate().withNewMetadata()
        .addToLabels("app", "quarkus").endMetadata().withNewSpec().addNewContainer()
        .withName("quarkus").withImage("wilda/" + repoName + ":" + currentRelease).addNewPort()
        .withContainerPort(80).endPort().endContainer().endSpec().endTemplate().endSpec().build();

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
    Service service = new ServiceBuilder().withNewMetadata().withName("quarkus-service")
        .addToLabels("app", "quarkus").endMetadata().withNewSpec().withType("NodePort")
        .withSelector(Map.of("app", "quarkus")).addNewPort().withPort(80)
        .withTargetPort(new IntOrString(8080)).withNodePort(30080).endPort().endSpec().build();

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

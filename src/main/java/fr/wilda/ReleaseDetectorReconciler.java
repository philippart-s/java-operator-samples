package fr.wilda;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;

public class ReleaseDetectorReconciler implements Reconciler<ReleaseDetector> { 
  private final KubernetesClient client;

  public ReleaseDetectorReconciler(KubernetesClient client) {
    this.client = client;
  }

  // TODO Fill in the rest of the reconciler

  @Override
  public UpdateControl<ReleaseDetector> reconcile(ReleaseDetector resource, Context context) {
    // TODO: fill in logic

    return UpdateControl.noUpdate();
  }
}


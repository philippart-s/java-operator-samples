package fr.wilda;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1")
@Group("wilda.fr")
public class ReleaseDetector extends CustomResource<ReleaseDetectorSpec, ReleaseDetectorStatus> implements Namespaced {}


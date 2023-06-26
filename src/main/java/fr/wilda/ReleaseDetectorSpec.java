package fr.wilda;

/**
 * Spec part of the custom resource definition.
 * Store the flag to allow or not the operator to deploy the Quarkus application.
 */
public class ReleaseDetectorSpec {

    private String deploy;

    public String getDeploy() {
        return deploy;
    }

    public void setDeploy(String deploy) {
        this.deploy = deploy;
    }
}
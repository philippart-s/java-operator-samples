package fr.wilda;

/**
 * Status part of the CRD. Store the deployed release version of the application.
 */
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

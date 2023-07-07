package fr.wilda;

/**
 * Spec part of the custom resource definition.
 * Allow to set the URL of the GitHub repository where check if a new release is created and if the release candidate are used.
 */
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
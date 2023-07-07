package fr.wilda.util;

import com.fasterxml.jackson.annotation.JsonProperty;

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
package org.pageseeder.sdk.service;

/**
 * Supported PageSeeder payload formats.
 *
 * <p>URI template helper for PageSeeder services.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public enum PayloadFormat {

  /**
   * Represents the CSV payload format with its associated media type and file extension.
   *
   * <p>This payload format is typically used for handling data in the "Comma-Separated Values" (CSV) format.
   * It specifies the media type as "text/csv" and the file extension as ".csv".
   */
  CSV("text/csv", ".csv"),

  /**
   * Represents the JSON payload format with its associated media type and file extension.
   *
   * <p>This payload format is used for handling data in the "JavaScript Object Notation" (JSON) format.
   * It specifies the media type as "application/json" and the file extension as ".json".
   */
  JSON("application/json", ".json"),

  /**
   * Represents the XML payload format with its associated media type and file extension.
   *
   * <p>This payload format is commonly used for handling data in the "Extensible Markup Language" (XML) format.
   * It specifies the media type as "application/xml" and the file extension as ".xml".
   */
  XML("application/xml", ".xml");

  private final String mediaType;

  private final String extension;

  PayloadFormat(String mediaType, String extension) {
    this.mediaType = mediaType;
    this.extension = extension;
  }

  /**
   * Return the media type associated with this payload format.
   *
   * @return the media type associated with this payload format.
   */
  public String mediaType() {
    return this.mediaType;
  }

  /**
   * Return the file extension associated with this payload format.
   *
   * @return the file extension associated with this payload format.
   */
  public String extension() {
    return this.extension;
  }

}

package org.pageseeder.sdk.oauth;

/**
 * OAuth 2.0 grant types supported by PageSeeder.
 */
public enum GrantType {

  /** Authorization-code grant. */
  AUTHORIZATION_CODE("authorization_code"),

  /** Implicit grant. */
  IMPLICIT("implicit"),

  /** Resource-owner password grant. */
  PASSWORD("password"),

  /** Client-credentials grant. */
  CLIENT_CREDENTIALS("client_credentials"),

  /** Refresh-token grant. */
  REFRESH_TOKEN("refresh_token");

  private final String parameterValue;

  GrantType(String parameterValue) {
    this.parameterValue = parameterValue;
  }

  /**
   * Returns the OAuth parameter value for this grant type.
   *
   * @return the value used in the {@code grant_type} parameter
   */
  public String parameterValue() {
    return this.parameterValue;
  }

  /**
   * Parses an OAuth {@code grant_type} parameter value.
   *
   * @param parameterValue the parameter value
   * @return the matching grant type
   */
  public static GrantType fromParameterValue(String parameterValue) {
    for (GrantType type : values()) {
      if (type.parameterValue.equals(parameterValue)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unsupported grant type: " + parameterValue);
  }
}

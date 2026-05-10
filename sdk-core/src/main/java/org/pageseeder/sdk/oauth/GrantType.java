package org.pageseeder.sdk.oauth;

/**
 * OAuth 2.0 grant types supported by PageSeeder.
 */
public enum GrantType {

  AUTHORIZATION_CODE("authorization_code"),
  IMPLICIT("implicit"),
  PASSWORD("password"),
  CLIENT_CREDENTIALS("client_credentials"),
  REFRESH_TOKEN("refresh_token");

  private final String parameterValue;

  GrantType(String parameterValue) {
    this.parameterValue = parameterValue;
  }

  public String parameterValue() {
    return this.parameterValue;
  }

  public static GrantType fromParameterValue(String parameterValue) {
    for (GrantType type : values()) {
      if (type.parameterValue.equals(parameterValue)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unsupported grant type: " + parameterValue);
  }
}

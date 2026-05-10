package org.pageseeder.sdk.legacy;

import org.junit.jupiter.api.Test;
import org.pageseeder.bridge.PSConfig;
import org.pageseeder.sdk.PageSeederInstance;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PSConfigsTest {

  @Test
  void toInstance_simpleUrl() {
    PSConfig config = PSConfig.newInstance("https://example.com");
    PageSeederInstance instance = PSConfigs.toInstance(config);

    assertEquals("https://example.com",  instance.websiteOrigin().toString());
    assertEquals("https://example.com",  instance.apiOrigin().toString());
    assertEquals("http://example.com",   instance.documentOrigin().toString());
    assertEquals("/ps",                  instance.sitePrefix());
  }

  @Test
  void toInstance_separateApiUrl() {
    PSConfig config = PSConfig.newInstance("https://example.com", "https://api.example.com");
    PageSeederInstance instance = PSConfigs.toInstance(config);

    assertEquals("https://example.com",      instance.websiteOrigin().toString());
    assertEquals("https://api.example.com",  instance.apiOrigin().toString());
    assertEquals("/ps",                      instance.sitePrefix());
  }

  @Test
  void toInstance_nonDefaultPorts() {
    PSConfig config = PSConfig.newInstance("http://localhost:8080", "http://localhost:8282");
    PageSeederInstance instance = PSConfigs.toInstance(config);

    assertEquals("http://localhost:8080",  instance.websiteOrigin().toString());
    assertEquals("http://localhost:8282",  instance.apiOrigin().toString());
    assertEquals("http://localhost",       instance.documentOrigin().toString());
  }

  @Test
  void toInstance_defaultPortsOmitted() {
    PSConfig config = PSConfig.newInstance("https://example.com:443");
    PageSeederInstance instance = PSConfigs.toInstance(config);

    assertEquals("https://example.com",  instance.websiteOrigin().toString());
    assertEquals("https://example.com",  instance.apiOrigin().toString());
  }

  @Test
  void toInstance_customSitePrefix() {
    // PSConfig doesn't expose a prefix setter via newInstance(String),
    // so use the Properties-based factory which does support siteprefix
    Properties p = new Properties();
    p.setProperty("url", "https://example.com");
    p.setProperty("siteprefix", "/pageseeder");
    PSConfig configWithPrefix = PSConfig.newInstance(p);
    PageSeederInstance instance = PSConfigs.toInstance(configWithPrefix);

    assertEquals("/pageseeder", instance.sitePrefix());
  }
}

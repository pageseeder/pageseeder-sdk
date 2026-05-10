plugins {
  `java-library`
  `maven-publish`
  alias(libs.plugins.cyclonedx)
}

description = "PageSeeder SDK core — HTTP client, service catalog, OAuth, XML parsing"

dependencies {
  api(libs.slf4j.api)

  compileOnly(libs.jspecify)

  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.slf4j.simple)
  testRuntimeOnly(libs.junit.platform.launcher)
}

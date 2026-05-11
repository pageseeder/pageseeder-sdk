plugins {
  `java-library`
  `maven-publish`
  alias(libs.plugins.cyclonedx)
}

description = "PageSeeder SDK model — domain types and Jackson-based decoders"

dependencies {
  api(project(":sdk-core"))
  api(libs.jackson.databind)
  api(libs.jackson.dataformat.xml)
  api(libs.jackson.datatype.jsr310)

  compileOnly(libs.jspecify)

  testImplementation(libs.junit.jupiter)
  testImplementation(libs.slf4j.simple)
  testRuntimeOnly(libs.junit.platform.launcher)
}

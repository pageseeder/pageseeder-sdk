plugins {
  `java-library`
  `maven-publish`
  alias(libs.plugins.cyclonedx)
}

description = "PageSeeder SDK legacy — adapters to help transition from PageSeeder Bridge to the SDK"

tasks.javadoc {
  // sdk-legacy is a placeholder; suppress failure when no public classes are present yet
  (options as StandardJavadocDocletOptions).addBooleanOption("quiet", true)
  isFailOnError = false
}

dependencies {
  api(project(":sdk-core"))
  api(libs.pso.bridge)

  compileOnly(libs.jspecify)

  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.platform.launcher)
}

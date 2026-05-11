plugins {
  application
  `maven-publish`
  alias(libs.plugins.cyclonedx)
}

description = "PageSeeder SDK CLI — command-line tools"

application {
  mainClass.set("org.pageseeder.sdk.cli.Main")
}

dependencies {
  compileOnly(libs.jspecify)

  implementation(project(":sdk-core"))
  implementation(project(":sdk-model"))

  testImplementation(libs.junit.jupiter)
  testImplementation(libs.slf4j.simple)
  testRuntimeOnly(libs.junit.platform.launcher)
}

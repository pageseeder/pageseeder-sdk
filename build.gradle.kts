import org.gradle.api.publish.maven.MavenPublication
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
  alias(libs.plugins.jreleaser) apply false
  alias(libs.plugins.cyclonedx) apply false
  alias(libs.plugins.sonar)
}

val globalVersion = file("version.txt").readText().trim()

sonar {
  properties {
    property("sonar.projectKey", "pageseeder_pageseeder-sdk")
    property("sonar.organization", "pageseeder")
    // Tell SonarCloud where the JaCoCo XML reports are
    property(
      "sonar.coverage.jacoco.xmlReportPaths",
      subprojects.joinToString(",") {
        it.layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml").get().asFile.absolutePath
      }
    )
  }
}

subprojects {
  group   = "org.pageseeder.sdk"
  version = globalVersion

  repositories {
    mavenCentral()
  }

  pluginManager.withPlugin("org.cyclonedx.bom") {
    tasks.named("cyclonedxBom") {
      setProperty("includeConfigs", listOf("runtimeClasspath"))
      setProperty("outputFormat", "json")
    }
    tasks.named("assemble") {
      dependsOn(tasks.named("cyclonedxBom"))
    }
    pluginManager.withPlugin("maven-publish") {
      configure<PublishingExtension> {
        publications.named<MavenPublication>("maven") {
          artifact(layout.buildDirectory.file("reports/bom.json")) {
            classifier = "cyclonedx"
            extension = "json"
            builtBy(tasks.named("cyclonedxBom"))
          }
        }
      }
    }
  }

  pluginManager.withPlugin("java") {
    apply(plugin = "jacoco")

    configure<JavaPluginExtension> {
      toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
      }
    }

    val jacocoTestReport = tasks.named<JacocoReport>("jacocoTestReport") {
      dependsOn(tasks.named("test"))
      reports {
        xml.required.set(true)
      }
    }

    rootProject.tasks.named("sonar") {
      dependsOn(jacocoTestReport)
    }

    tasks.withType<Test> {
      finalizedBy(jacocoTestReport)
    }
  }

  pluginManager.withPlugin("java-library") {
    configure<JavaPluginExtension> {
      withJavadocJar()
      withSourcesJar()
    }
  }

  pluginManager.withPlugin("maven-publish") {
    configure<PublishingExtension> {
      publications {
        create<MavenPublication>("maven") {
          from(components["java"])
          artifactId = project.name

          pom {
            val title = project.findProperty("title")?.toString() ?: project.name
            val website = project.findProperty("website")?.toString() ?: ""
            val gitName = project.findProperty("gitName")?.toString() ?: project.name

            name.set(title)
            description.set(project.description ?: title)
            url.set(website)
            licenses {
              license {
                name.set("The Apache Software License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
              }
            }
            organization {
              name.set("Allette Systems")
              url.set("https://www.allette.com.au")
            }
            scm {
              url.set("git@github.com:pageseeder/${gitName}.git")
              connection.set("scm:git:git@github.com:pageseeder/${gitName}.git")
              developerConnection.set("scm:git:git@github.com:pageseeder/${gitName}.git")
            }
            developers {
              developer {
                name.set("Christophe Lauret")
                email.set("clauret@weborganic.com")
              }
            }
          }
        }
      }

      repositories {
        maven {
          url = rootProject.layout.buildDirectory.dir("staging-deploy/${project.name}").get().asFile.toURI()
        }
      }
    }
  }

  tasks.withType<Test> {
    useJUnitPlatform()
  }

  tasks.withType<Javadoc> {
    options { encoding = "UTF-8" }
  }
}

tasks.register<Delete>("clean") {
  group = "build"
  description = "Deletes the build directory."
  delete(layout.buildDirectory)
}

tasks.wrapper {
  gradleVersion = "8.14.4"
  distributionType = Wrapper.DistributionType.ALL
}

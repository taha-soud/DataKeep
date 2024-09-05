plugins {
    java
    application
}

java {
    application {
        mainClass.set("Application")
    }
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.api-client:google-api-client:2.0.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.google.apis:google-api-services-drive:v3-rev20220815-2.0.0")
    implementation("com.itextpdf:itextpdf:5.5.13.3")

}


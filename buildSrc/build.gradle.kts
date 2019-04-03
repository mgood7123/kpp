plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        register("KOTLIN_PRE_PROCESSOR") {
            id = "KOTLIN_PRE_PROCESSOR"
            implementationClass = "kpp"
        }
    }
}

repositories {
    jcenter()
}
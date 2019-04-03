plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        register("greet-plugin") {
            id = "greet"
            implementationClass = "GreetPlugin"
        }
        register("KOTLIN_PRE_PROCESSOR") {
            id = "KOTLIN_PRE_PROCESSOR"
            implementationClass = "kpp"
        }
    }
}

repositories {
    jcenter()
}
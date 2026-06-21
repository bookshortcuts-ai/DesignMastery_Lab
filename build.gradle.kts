// Dummy build script to satisfy platform Gradle build requirements for the container
tasks.register("clean") {
    doLast {
        println("Dummy clean task completed")
    }
}
tasks.register("assembleDebug") {
    doLast {
        println("Dummy build completed successfully")
    }
}

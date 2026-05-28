// Top-level build file — plugins are declared in settings.gradle.kts
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

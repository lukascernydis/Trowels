ModsDotGroovy.make {
    modLoader = 'javafml'
    loaderVersion = '[43,)'

    license = 'MIT'
    issueTrackerUrl = 'https://github.com/lukascernydis/Trowels/issues'

    mod {
        modId = 'trowelsfork'
        displayName = 'Trowels Fork'

        version = this.version

        description = 'Fork of the Trowels mod with some fixes.'
        authors = ['Matyrobbrt', 'CatAndPaste', 'Luciano']

        logoFile = 'trowels.png'

        dependencies {
            forge = "[${this.forgeVersion},)"
            minecraft = this.minecraftVersionRange
        }
    }
}
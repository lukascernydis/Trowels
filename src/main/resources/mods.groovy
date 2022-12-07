ModsDotGroovy.make {
    modLoader = 'javafml'
    loaderVersion = '[43,)'

    license = 'MIT'
    issueTrackerUrl = 'https://github.com/MatyrobbrtMods/Trowels/issues'

    mod {
        modId = 'trowels'
        displayName = 'Trowels'

        version = this.version

        description = 'A mod adding trowels used for decoration.'
        authors = ['Matyrobbrt']

        logoFile = 'trowels.png'

        dependencies {
            forge = "[${this.forgeVersion},)"
            minecraft = this.minecraftVersionRange
        }
    }
}
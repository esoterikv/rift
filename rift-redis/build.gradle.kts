plugins {
    `rift-java`
    `rift-repositories`
}

dependencies {
    api(project(":rift-common"))
    api("io.lettuce:lettuce-core:6.5.5.RELEASE")
}
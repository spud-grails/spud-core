grails.project.work.dir = 'target'
grails.project.target.level = 1.6
grails.project.source.level = 1.6

grails.project.fork = [
    test: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
    // configure settings for the run-app JVM
    run: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    // configure settings for the run-war JVM
    war: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    // configure settings for the Console UI JVM
    console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]


grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
    inherits "global"
    log "warn"
    repositories {
        grailsCentral()
        grailsPlugins()
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        compile 'com.github.jknack:handlebars:1.3.0'
    }

    plugins {
        runtime ":asset-pipeline:2.1.5"
        runtime ":retina-tag:2.1.1"
        runtime ":coffee-asset-pipeline:2.0.6"
        runtime ":security-bridge:0.5.4"
        runtime ":sitemaps:1.0.0"
        runtime ':cache:1.1.7'
        if(System.getProperty('plugin.mode') != 'local') {

            runtime(':hibernate4:4.3.5.4') {
                export = false
            }

            build ":tomcat:7.0.52.1"
            build(":release:3.0.1",
                  ":rest-client-builder:1.0.3") {
                export = false
            }
        }
    }
}

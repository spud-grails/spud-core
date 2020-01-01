package spud.core

import grails.config.Config
import grails.core.DefaultGrailsApplication
import org.grails.config.PropertySourcesConfig

class AdminApplicationService {
	static transactional = false
	def grailsApplication
	def sharedSecurityService
	def spudCustomFieldService

	def initialize() {
		def adminApplications = loadAdminApplications()
		Config config = grailsApplication.config
		def configProperties = config.toProperties()
		configProperties.propertyNames().each { propName ->
			def value = configProperties.get(propName)
			log.trace "${propName}:${value} ${value.class.simpleName}"
		}
		def spudCoreConfig = config.getProperty("spud.core.adminApplications")
		log.debug "initialize spudCoreConfig before: ${spudCoreConfig}"
		if(!spudCoreConfig) {
			def tmpApplication = new DefaultGrailsApplication()
			def updatedConfig = tmpApplication.config
			updatedConfig.setAt("spud.core.adminApplications", adminApplications)
			log.debug "tmpConfig: ${updatedConfig}"
			grailsApplication.config.merge(updatedConfig)
		}
		log.debug "initialize spudCoreConfig after: ${grailsApplication.config.spud.core.adminApplications}"
		spudCustomFieldService.loadCustomFieldsFromConfig()
	}

	def myApplications() {
		log.debug "myApplications grailsApplication.config.spud.core.adminApplications: ${grailsApplication.config.spud.core.adminApplications}"
		def tmpApplications = loadAdminApplications()
//		def apps = grailsApplication.config.spud.core.adminApplications.findAll {app ->
		def apps = tmpApplications.findAll { app ->
			app.roles.find { role -> sharedSecurityService.hasRole("SPUD_${role}") }
		}
		log.debug "myApplications apps: ${apps}"
		apps
	}

	private isEnabled(annotation) {
		log.debug "isEnabled annotation: ${annotation}"
		if(!annotation.enabled()) {
			return true
		}

		def config = grailsApplication.config
		def enabledArgs = annotation.enabled().toString().tokenize(".")
		def configMap = config
		enabledArgs.each { arg ->
			configMap = configMap."${arg}"
		}
		if(configMap == true) {
			return true
		} else if(configMap == false) {
			return false
		} else {
			return annotation.defaultEnabled().toBoolean()
		}

	}

	private def adminMapFromAnnotation(annotation, controllerClass) {
		def rtn = [:]
		rtn.name = annotation.name()
		rtn.thumbnail = annotation.thumbnail()
		rtn.order = annotation.order().toInteger()
		rtn.url = [controller: controllerClass.logicalPropertyName, action: 'index']
		rtn.roles = controllerClass.clazz.getAnnotation(spud.core.SpudSecure)?.value() ?: []

		if(controllerClass.getPropertyValue('namespace') != 'spud_admin') {
			log.warn "WARNING! SpudApp controller ${controllerClass.name} should be in the 'spud_admin' namespace!"
		}
		log.debug "adminMapFromAnnotation controllerClass: ${controllerClass?.dump()}"
		log.debug "adminMapFromAnnotation rtn: ${rtn}"
		return rtn
	}

	def loadAdminApplications() {
		def tmpApplications = []

		grailsApplication.controllerClasses.each { controllerClass ->
			def annotation = controllerClass.clazz.getAnnotation(spud.core.SpudApp)
			log.debug "loadAdminApplications controllerClass: ${controllerClass}"
			log.trace "loadAdminApplications controllerClass: ${controllerClass?.dump()}"
			if(annotation && annotation.subsection() == 'false') {
				if(isEnabled(annotation)) {
					log.debug "loadAdminApplications annotation is enabled. ${annotation}"
					tmpApplications << adminMapFromAnnotation(annotation, controllerClass)
				} else {
					log.debug "loadAdminApplications annotation is NOT enabled. ${annotation}"
				}
			}
		}

		tmpApplications = tmpApplications.sort { it.order?.toInteger() }
		tmpApplications
	}
}

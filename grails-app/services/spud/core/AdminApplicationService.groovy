package spud.core

import grails.transaction.Transactional

@Transactional
class AdminApplicationService {
	def grailsApplication
	def initialize() {
		def adminApplications = []

		grailsApplication.controllerClasses.each { controllerClass ->
			def annotation = controllerClass.clazz.getAnnotation(spud.core.SpudApp)
			if(annotation && annotation.subsection() == 'false') {
				if(isEnabled(annotation)) {
					adminApplications << adminMapFromAnnotation(annotation, controllerClass)	
				}
				
			}
		}

		grailsApplication.config.spud.core.adminApplications = adminApplications.sort{ it.order?.toInteger() }
	}


	private isEnabled(annotation) {
		if(!annotation.enabled()) {
			return true
		}

		def config = grailsApplication.config
		def enabledArgs = annotation.enabled().split(".")
		enabledArgs.each { arg ->
			config = config."${arg}"
		}
		if(config == true) {
			return true
		} else if (config == false) {
			return false
		} else {
			return annotation.defaultEnabled().toBoolean()
		}

	}

	private def adminMapFromAnnotation(annotation, controllerClass) {
		def rtn       = [:]
		rtn.name      = annotation.name()
		rtn.thumbnail = annotation.thumbnail()
		rtn.order     = annotation.order().toInteger()
		rtn.url       = [controller: controllerClass.logicalPropertyName, action: 'index']
		return rtn
	}
}

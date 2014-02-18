package spud.core
import grails.util.GrailsWebUtil
class SecurityFilters {

	def grailsApplication
	def sharedSecurityService

	def filters = {

		/**
		* Filters all admin url mappings and verifies restrictions in controller defined by the @SpudSecure annotation
		*/
		spudAdmin(uri: '/spud/admin/**') {
			before = {
				def controllerClass = grailsApplication.getArtefactByLogicalPropertyName("Controller", controllerName)
				
				def action
				if(controllerClass) {
	                action = controllerClass.clazz.declaredMethods.find { it.name == actionName }
	                if(!action) {
	                	action = applicationContext.getBean(controllerClass.fullName).class.declaredFields.find { field -> field.name == actionName }	
	                }
				}
				def annotation = action?.getAnnotation(SpudSecure)

				if(!annotation && controllerClass) {
					annotation = controllerClass.clazz.getAnnotation(SpudSecure)
				}

				if(!annotation) {
					return true  //No Security Restrictions
				}

				if(!sharedSecurityService.hasAnyRole(annotation.value())) {
					sharedSecurityService.storeLocation(request)
					redirect(sharedSecurityService.createLink('login'))
					return false
				}
				return true
			}
		}
	}
}

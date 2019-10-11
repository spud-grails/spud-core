package spud.core

class SpudCustomFieldService {
	static transactional = false

	def grailsApplication

	def customFieldsForSite(siteId = 0, typeSetName) {
		//TODO We need to make this support multisite

		def typeSet = typeSets[typeSetName]
		return typeSet
	}

	def typeSets = [:]

	def loadCustomFieldsFromConfig() {
		customFieldConfig.each { customFieldSet ->
			def fieldConfig = customFieldSet.value
			typeSets[customFieldSet.key] = fieldConfig.collect { fieldMeta ->
				log.debug "Loading Special Field Meta ${fieldMeta.value + [name: fieldMeta.key]}"
				new SpudCustomField(fieldMeta.value + [name: fieldMeta.key])
			}
		}
	}

	protected getCustomFieldConfig() {
		grailsApplication.config.spud.customFieldSets
	}
}

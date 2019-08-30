package spud.core

import org.grails.web.util.GrailsApplicationAttributes

class SpudAdminTagLib {
    static defaultEncodeAs = 'html'
    static namespace = 'spAdmin'
    static encodeAsForTags = [logoutLink: 'raw', breadcrumbs: 'raw', pageThumbnail:'raw', link:'raw', formatterSelect: 'raw', multiSiteSelect: 'raw', multiSiteEnabled: 'raw', withActiveSite: 'raw',hasCustomFields: 'raw',customFieldSet: 'raw']

    def sharedSecurityService
    def spudMultiSiteService
    def spudCustomFieldService

    def currentUserDisplayName = {
    	out << sharedSecurityService.currentUserDisplayName
    }

    def formatterSelect = {attrs ->
		log.debug "formatterSelect attrs before: ${attrs}"
        def config = grailsApplication.config.spud
		log.debug "formatterSelect config: ${config}"
        def formatters = config.formatters
		log.debug "formatterSelect formatters: ${formatters}"
        attrs.from=formatters
        attrs.optionKey = 'name'
        attrs.optionValue = 'description'
		log.debug "formatterSelect attrs after: ${attrs}"
        out << g.select(attrs)
    }

    def multiSiteSelect = {attrs ->
        def sites = spudMultiSiteService.availableSites()
        attrs.from = sites
        attrs.optionKey = 'siteId'
        attrs.optionValue = 'name'
        attrs.value = spudMultiSiteService.activeSite.siteId
        out << g.select(attrs)
    }

    def withActiveSite = {attrs, body ->
        out << body(site: spudMultiSiteService.activeSite.siteId)
    }

    def pageThumbnail = { attrs ->
        def controllerClass = request.getAttribute(GrailsApplicationAttributes.GRAILS_CONTROLLER_CLASS)

        // def controllerClass = grailsApplication.getArtefactByLogicalPropertyName('Controller', pageScope.controllerName)
        def annotation = controllerClass.clazz.getAnnotation(spud.core.SpudApp)
        if(annotation) {
            attrs = attrs + [src: annotation.thumbnail()]
            out << asset.image(attrs)
        }
    }

    def pageName = { attrs ->
        def controllerClass = request.getAttribute(GrailsApplicationAttributes.GRAILS_CONTROLLER_CLASS)
        def annotation = controllerClass.clazz.getAnnotation(spud.core.SpudApp)
        if(annotation) {
            if(annotation.subsection() != "false") {
                out << annotation.subsection()
            } else {
                out << annotation.name()
            }

        }
    }


    /**
    * Taglib block that only renders if multiSite is enabled
    * TODO: Modify this to support a permissions level check as well
    */
    def multiSiteEnabled = {attrs, body ->
        if(spudMultiSiteService.isMultiSiteEnabled()) {
            out << body()
        }
    }

    def settingsLink = { attrs, body ->
        def settingsUrl = sharedSecurityService.createLink('settings')
        if(settingsUrl) {
            attrs = attrs + settingsUrl
            out << g.link(attrs,body)
        }
    }

    def logoutLink = { attrs, body ->
    	attrs = attrs + sharedSecurityService.createLink('logout')
    	out << g.link(attrs,body)
    }

    def breadcrumbs = { attrs ->
        def crumbLinks      = []
        def controllerClass = grailsApplication.getArtefactByLogicalPropertyName('Controller', pageScope.controllerName)
        def annotation      = controllerClass.clazz.getAnnotation(spud.core.SpudApp)
        def parentController
        crumbLinks << link([controller: 'dashboard', action: 'index'],"Dashboard")
        if(annotation && annotation.subsection() != "false") {
            parentController = grailsApplication.controllerClasses.find { controllerArtefact ->
                def parentAnno = controllerArtefact.clazz.getAnnotation(spud.core.SpudApp)
                if(parentAnno && parentAnno.name() == annotation.name() && parentAnno.subsection() == "false") {
                    def linkOptions = [resource: controllerArtefact.logicalPropertyName, action: 'index']

                    crumbLinks << link(linkOptions, "${parentAnno.name()}")
                    return true
                }
            }
            // Todo Find Root Controller
        }
        if(annotation) {
            if(pageScope.actionName == 'index') {
                crumbLinks << "${annotation.subsection() != 'false' ? annotation.subsection(): annotation.name()}"
            } else {
                def linkOptions = [resource: pageScope.controllerName, action: 'index', namespace: 'spud_admin']
                if(parentController && params[parentController.logicalPropertyName + "Id"]) {
                    linkOptions[parentController.logicalPropertyName + "Id"] = params[parentController.logicalPropertyName + "Id"]
                    linkOptions.resource = parentController.logicalPropertyName + "/" + pageScope.controllerName
                }
                crumbLinks << link(linkOptions, "${annotation.subsection() != 'false' ? annotation.subsection(): annotation.name()}")
            }
        }
        if(pageScope.actionName != 'index') {
            crumbLinks << pageScope.actionName.titlecase() //TODO: Title Case this
        }

        out << crumbLinks.join("&nbsp;/&nbsp;")
    }

    def link = { attrs, body ->
        out << g.link(attrs + [namespace: 'spud_admin'],body)
    }

    def createLink = { attrs, body ->
        out << g.createLink(attrs + [namespace: 'spud_admin'],body)
    }

    def hasCustomFields = { attrs, body ->
        def customFields = spudCustomFieldService.customFieldsForSite(attrs.type)
        if(customFields) {
            out << body()
        }
    }

    def customFieldSet = { attrs ->
        def typeSetName = attrs.type
        def objectType = attrs.objectType
        def object = attrs.object
        def objectField = attrs.objectField

        def customFields = spudCustomFieldService.customFieldsForSite(typeSetName)

        out << g.render(plugin:"spud-core", template: '/spud/admin/custom/fields', model: [objectType: objectType, objectField: objectField, customFields: customFields, object: object])

    }
}

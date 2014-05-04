package spud.core
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes

class SpudAdminTagLib {
    static defaultEncodeAs = 'html'
    static namespace = 'spAdmin'
    static encodeAsForTags = [logoutLink: 'raw', breadcrumbs: 'raw', pageThumbnail:'raw', link:'raw', formatterSelect: 'raw']

    def grailsApplication
    def sharedSecurityService

    def currentUserDisplayName = {
    	out << sharedSecurityService.currentUserDisplayName
    }

    def formatterSelect = {attrs ->
        def config = grailsApplication.config.spud
        def formatters = config.formatters
        attrs.from=formatters
        attrs.optionKey = 'name'
        attrs.optionValue = 'description'
        out << g.select(attrs)
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
}

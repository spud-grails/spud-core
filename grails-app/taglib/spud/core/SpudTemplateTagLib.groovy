package spud.core
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes

class SpudTemplateTagLib {
	static defaultEncodeAs = 'html'
	static namespace = 'sp'
	static encodeAsForTags = [handlebarsEscape: 'raw']

	def handlebarsEscape = { attrs, body ->
		//TODO: We need to figure out some trickery here
	}
}

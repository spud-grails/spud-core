package spud.core
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine
import org.codehaus.groovy.grails.web.pages.FastStringWriter

class SpudTemplateService {
  static transactional = false
	def grailsApplication
  def groovyPagesTemplateEngine
  // def groovyPageRenderer

  def render(name, content, options=[:]) {
    def contentToModify = new String(content)

    contentToModify = contentToModify.replaceAll(/\{\{#(.*)\}\}/) { fullMatch, tag ->
      def newTag = tag.trim()
      newTag = "<sp:${newTag} >"
      return newTag
    }

    contentToModify = contentToModify.replaceAll(/\{\{\/(.*)\}\}/) { fullMatch, tag ->
      def newTag = tag.trim()
      newTag = "</sp:${newTag}>"
      return newTag
    }
    
    contentToModify = contentToModify.replaceAll(/\{\{(.*)\}\}/) { fullMatch, tag ->
      def newTag = tag.trim()
      newTag = "<sp:${newTag} />"
      return newTag
    }
    

    def fsw = new FastStringWriter()
    println "About to Compile: \n${contentToModify}\n"
    groovyPagesTemplateEngine.createTemplate(contentToModify, name).make(options.model).writeTo(fsw)
    return fsw.toString()

    // groovyPageRenderer.render()
    // Time to do some regex magic
  }


  def layoutServiceForSite(siteId=0) {
    return layoutServiceByName('system')
  }

  def activeLayoutService(name = 'system') {
    if(name) {
      return layouterviceByName(name)
    }

    return layoutServiceByName('system')
  }

  private layoutServiceByName(key) {
    def engineName = grailsApplication.config.spud.layoutEngines[key]
    if(engineName) {
      return grailsApplication.mainContext[engineName]
    } else {
      return null
    }
  }
}

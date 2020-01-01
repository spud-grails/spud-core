package spud.core

import com.github.jknack.handlebars.*
import org.grails.buffer.FastStringWriter
import org.grails.taglib.encoder.OutputContext
import org.grails.taglib.encoder.OutputContextLookupHelper
import org.grails.taglib.encoder.OutputEncodingStack
import org.grails.taglib.encoder.OutputEncodingStackAttributes
import org.springframework.web.context.request.RequestContextHolder

class SpudTemplateService {
	static transactional = false
	def grailsApplication
	def groovyPagesTemplateEngine
	def gspTagLibraryLookup

	def render(name, content, options = [:]) {
		if(log.isDebugEnabled()) log.debug "render name: ${name}"
		if(log.isDebugEnabled()) log.debug "render options: ${options}"
		if(log.isTraceEnabled()) log.trace "render content: ${content}"
		if(!content) {
			return content
		}
		def startTime = new Date().time
		if(log.isTraceEnabled()) log.trace "render startTime: ${startTime}"
		def fsw = new FastStringWriter()
		def results
		def output = initStack(fsw)
		try {
			Template template = this.getHandlebars(fsw).compileInline(content)
			def binding = options?.model ?: [:]
			binding.params = RequestContextHolder?.requestAttributes?.params
			binding.parent = binding.params
			results = template.apply(options?.model ?: [:])
		} finally {
			cleanup(output)
		}
		return results
	}

	def getHandlebars(initialFsw) {
		def writers = [initialFsw]
		def handlebars = new Handlebars();
		handlebars.registerHelper(Handlebars.HELPER_MISSING, new Helper<Object>() {
			@Override
			public CharSequence apply(final Object context, final Options options) throws IOException {
				def helperFsw = new FastStringWriter()
				def output = initStack(helperFsw)
				writers.push(helperFsw)

				def fsw = writers[-1]
				def tagLib = gspTagLibraryLookup.lookupTagLibrary('sp', options.helperName)
				try {
					if(tagLib) {

						def tagName = options.helperName
						def tagMap = options.hash
						def tag = tagLib.getProperty(tagName).clone()
						def result
						if(tag.getParameterTypes().length == 1) {
							result = tag.call(tagMap)
							OutputEncodingStack.currentStack().flushActiveWriter()
						} else {
							def body = { newContext ->
								def newFsw = new FastStringWriter()
								writers.push(newFsw)
								def newOutput = initStack(newFsw)
								def content
								try {
									content = options.fn(newContext ?: context)
								} finally {
									newFsw.close()
									cleanup(newOutput)
									writers.pop();
								}
								return content


							}
							tag.call(tagMap, body)

						}
						if(!(result instanceof String)) {
							result = fsw.toString()
						}
						fsw.close()

						return result

					}
				}
				finally {
					cleanup(output)
					writers.pop()
				}
				return result ?: options.fn.text();
			}
		});
		/*registerTagLibraryHelpers('sp')*/
		return handlebars
	}

	/*private registerTagLibraryHelpers(namespace) {
		try {
			gspTagLibraryLookup.getAvailableTags(namespace).each { tagName ->
				def tagLib = gspTagLibraryLookup.lookupTagLibrary(namespace,tagName)
				handlebars.registerHelper(tagName, new Helper<Object>() {
					public CharSequence apply(Object context, Options options) {
						def tagMap = options.hash
						def body = { newContext ->
							return options.fn(newContext ?: context)
						}
						def tag = tagLib."${tagName}"
						if(tag.getParameterTypes().length == 1)
						{
							tagLib."${tagName}"(tagMap)
						} else {
							tagLib."${tagName}"(tagMap,body)
						}
					}
				})
			}
		} catch(e) {
			//For Grails 2.4.0 we attempt the new taglib resolver for speed
		}

	}*/

	protected initStack(Writer target) {
		def grailsWebRequest = RequestContextHolder.currentRequestAttributes()
		if(log.isDebugEnabled()) log.debug "initStack grailsWebRequest: ${grailsWebRequest}"
		OutputEncodingStackAttributes.Builder attributesBuilder = new OutputEncodingStackAttributes.Builder()
		if(log.isDebugEnabled()) log.debug "initStack attributesBuilder: ${attributesBuilder}"
//        GroovyPageOutputStackAttributes.Builder attributesBuilder = new GroovyPageOutputStackAttributes.Builder();

		OutputContext outputContext = OutputContextLookupHelper.lookupOutputContext()
		if(log.isDebugEnabled()) log.debug "initStack outputContext: ${outputContext}"
		attributesBuilder.outputContext(outputContext)
//		if (outputContext != null) {
//			return outputContext.getBinding();
//		}

		attributesBuilder.allowCreate(true).topWriter(target).autoSync(false).pushTop(true)
		if(log.isDebugEnabled()) log.debug "initStack attributesBuilder: ${attributesBuilder?.dump()}"
//        attributesBuilder.webRequest(grailsWebRequest);
		attributesBuilder.inheritPreviousEncoders(false)
//		def outputStack = GroovyPageOutputStack.currentStack(attributesBuilder.build());
		def outputStack = OutputEncodingStack.currentStack(attributesBuilder.build())
		if(log.isDebugEnabled()) log.debug "initStack outputStack: ${outputStack}"
//        grailsWebRequest.setOut(outputStack.getOutWriter());
		return outputStack
	}

	protected cleanup(outputStack) {
		outputStack?.pop(true);
	}

	def layoutServiceForSite(siteId = 0) {
		return layoutServiceByName('system')
	}

	def activeLayoutService(name = 'system') {
		if(name) {
			return layoutServiceByName(name)
		}

		return layoutServiceByName('system')
	}

	private layoutServiceByName(key) {
		def engineName = grailsApplication.config.spud.layoutEngines[key]
		if(engineName) {
			return grailsApplication.mainContext[engineName]
		} else {
			return grailsApplication.mainContext['defaultSpudLayoutService']
//			return null
		}
	}
}

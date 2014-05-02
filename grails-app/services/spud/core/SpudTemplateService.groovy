package spud.core
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine
import org.codehaus.groovy.grails.web.pages.FastStringWriter
import com.github.jknack.handlebars.*
import org.codehaus.groovy.grails.web.pages.GroovyPageOutputStack
import org.codehaus.groovy.grails.web.pages.GroovyPageOutputStackAttributes
import org.springframework.web.context.request.RequestContextHolder


class SpudTemplateService {
    static transactional = false
    def grailsApplication
    def groovyPagesTemplateEngine
    def gspTagLibraryLookup

    def render(name, content, options=[:]) {
        if(!content) {
            return content
        }
        def startTime = new Date().time
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

    def getHandlebars(fsw) {
        def handlebars = new Handlebars();
        handlebars.registerHelper(Handlebars.HELPER_MISSING, new Helper<Object>() {
            @Override
            public CharSequence apply(final Object context, final Options options) throws IOException {
                def tagLib = gspTagLibraryLookup.lookupTagLibrary('sp',options.helperName)
                if(tagLib) {
                    def tagName = options.helperName
                    def tagMap = options.hash
                    def body = options.fn.text()
                    def tag = tagLib."${tagName}"
                    if(tag.getParameterTypes().length == 1)
                    {
                        tag.call(tagMap)
                    } else {
                        tag.call(tagMap,body)
                    }
                    def result = fsw.toString()
                    fsw.close()
                    return result

                }
                return options.fn.text();
            }
        });
        registerTagLibraryHelpers('sp')
        return handlebars
    }

    private registerTagLibraryHelpers(namespace) {
        try {
            gspTagLibraryLookup.getAvailableTags(namespace).each { tagName ->
                def tagLib = gspTagLibraryLookup.lookupTagLibrary(namespace,tagName)
                handlebars.registerHelper(tagName, new Helper<Object>() {
                    public CharSequence apply(Object context, Options options) {
                        def tagMap = options.hash
                        def body = options.fn.text()
                        def tag = tagLib."${tagName}"
                        if(tag.getParameterTypes().length == 1)
                        {
                            return tag.call(tagMap)
                        } else {
                            return tag.call(tagMap,body)
                        }
                    }
                })
            }
        } catch(e) {
            //For Grails 2.4.0 we attempt the new taglib resolver for speed
        }

    }

    protected initStack(Writer target) {
        def grailsWebRequest = RequestContextHolder.currentRequestAttributes();
        GroovyPageOutputStackAttributes.Builder attributesBuilder = new GroovyPageOutputStackAttributes.Builder();

        attributesBuilder.allowCreate(true).topWriter(target).autoSync(false).pushTop(true);
        attributesBuilder.webRequest(grailsWebRequest);
        attributesBuilder.inheritPreviousEncoders(false);
        def outputStack = GroovyPageOutputStack.currentStack(attributesBuilder.build());
        grailsWebRequest.setOut(outputStack.getOutWriter());
        return outputStack
    }

    protected cleanup(outputStack) {
        outputStack?.pop(true);
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

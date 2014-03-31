package spud.core

import org.apache.tools.ant.DirectoryScanner
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
import org.codehaus.groovy.grails.web.context.ServletContextHolder

class DefaultSpudLayoutService {
	static transactional = false
	def grailsApplication
	def dynamicOnWarDeploy = true
	def groovyPageResourceLoader
	def groovyPageLocator

	def currentSiteId() {
		return 0
	}

	def layoutsForSite(siteId=0) {
		def layouts = []
		if(grailsApplication.warDeployed) {
			layouts = grailsApplication.config.spud.core.layouts
		} else {
			layouts = scanForLayouts(layoutPaths())
		}

		return layouts
	}

	// Fetches The Actual Layout File Contents for parsing
	def layoutContents(name) {
		if(groovyPageResourceLoader) {
			if(layoutScript) {
				def layoutScript = groovyPageLocator.findPage("/layouts/${name}.gsp")	
				return groovyPageResourceLoader.getResource(layoutScript.URI)?.inputStream?.text
			}
		} else if(grailsApplication.warDeployed) {
		    def servletContext = ServletContextHolder.servletContext

			def resourcePaths = ["/WEB-INF/grails-app/views/"]
			
			resourcePaths += servletContext.getResourcePaths("/WEB-INF/plugins").collect { pluginResource ->
				return "${pluginResource}grails-app/views/"
			}
			
			for( resourcePath in resourcePaths ) {
				def layoutResource = grailsApplication.parentContext.getResource(resourcePath + "layouts/${name}.gsp")
				if(layoutResource.exists()) {
					return layoutResource.inputStream.text
				}
			}
			
		}
		
		
		return null
	}

	def layoutForName(name, siteId=0) {
		def layouts = layoutsForSite(siteId)
		return layouts.find {it.name == name}
	}

	def render(defaultView, options) {
		//Options available, view: 'file ref', content: 'content', model, objects to pass through
		return [view: defaultView] + options
	}

	private layoutPaths() {
		def paths = []
		paths << new File("grails-app/views/layouts").getAbsolutePath()

		for(plugin in GrailsPluginUtils.pluginInfos) {
			paths << [plugin.pluginDir.getPath(), "grails-app", "views/layouts"].join(File.separator)
		}
		return paths
	}

	private scanForLayouts(paths) {
		DirectoryScanner scanner = new DirectoryScanner()
		def filesToProcess       = []

		paths.each { path ->
			if(new File(path).exists()) {
				scanner.setIncludes(["**/*.gsp"] as String[])
				scanner.setBasedir(path)
				scanner.setCaseSensitive(false)
				scanner.scan()
				filesToProcess += scanner.getIncludedFiles().flatten()
			}
		}

		filesToProcess.unique()
		filesToProcess = filesToProcess.collect { fileName ->
			def extensionIndex = fileName.lastIndexOf('.gsp')
			return extensionIndex != -1 ? fileName.substring(0,extensionIndex) : fileName
		}

		return filesToProcess
	}

}

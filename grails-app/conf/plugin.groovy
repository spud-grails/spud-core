spud.formatters = [
	[name: 'html', description: 'Formatted HTML'],
	[name: 'raw', description: 'RAW HTML']
]

spud.renderers = grailsApplication.config.spud.renderers ?: [:]
spud.layoutEngines = grailsApplication.config.spud.layoutEngines ?: [:]
spud.renderers.gsp = 'defaultSpudRendererService'
spud.layoutEngines.system = 'defaultSpudLayoutService'

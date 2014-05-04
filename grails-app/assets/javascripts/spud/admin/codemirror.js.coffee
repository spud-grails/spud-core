#= require codemirror/lib/codemirror
#= require codemirror/mode/xml/xml
#= require codemirror/mode/javascript/javascript
#= require codemirror/mode/css/css
#= require codemirror/mode/htmlmixed/htmlmixed

class @SpudCodeMirror
	editor: null
	refreshTimeout: null
	defaultOptions:
		lineNumbers: true
		lineWrapping: true
		theme: 'default'
		mode:
			name: 'htmlmixed'

	constructor: (@element, options) ->
		if !@element.attr('id')
			@assignId()

		@loadWhenVisible()

	loadWhenVisible: () =>
		if @element.is(':visible')
			@refreshTimeout = null
			@editor = CodeMirror.fromTextArea(@element[0], @defaultOptions)
		else
			@refreshTimeout = setTimeout($.proxy(->
				@loadWhenVisible()
			,this),100)

	assignId: () =>
		baseId = "spud-codemirror-"
		counter = 1
		counter += 1 while $("##{baseId}#{counter}").length
		@element.attr 'id', "#{baseId}#{counter}"

	unload: ->
		if @refreshTimeout
			clearTimeout @refreshTimeout
		else
			@editor.toTextArea()

@spud.admin.editor.registerEditor 'raw', @SpudCodeMirror

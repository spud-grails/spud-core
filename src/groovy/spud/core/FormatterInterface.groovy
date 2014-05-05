package spud.core

/**
* Custom Formatter Interface for compiling text in a different format to HTML.
* (i.e. a MarkdownFormatter)
*/
interface FormatterInterface {
	/**
	* Method used for compiling text into a specific format
	*/
	String compile(String input)
}

package measure


data class ErrorInfo(val type: ParseError, val msg: String? = null, val line: Int? = null, val col: Int? = null)
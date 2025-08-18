package measure

enum class ParseErrorType { ADDED_TOKEN, REMOVED_TOKEN, CHANGED_TOKEN, UNKNOWN }

data class ParseError(
    val type: ParseErrorType,
    val affectedToken: String,
)

val UNKNOWN_ERROR = ParseError(ParseErrorType.UNKNOWN, "")
val MISSING_SEMICOLON = ParseError(ParseErrorType.REMOVED_TOKEN, ";")
val MISSING_ARROW = ParseError(ParseErrorType.REMOVED_TOKEN, "->")
val MISSING_OPEN_BRACKET = ParseError(ParseErrorType.REMOVED_TOKEN, "{")

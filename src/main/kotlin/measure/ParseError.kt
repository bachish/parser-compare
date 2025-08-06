package measure

enum class ParseError {
    NOT_A_STATEMENT,
    SEMICOLON_EXPECTED,
    OPEN_BRACKET_EXPECTED,
    ARROW_EXPECTED,
    UNKNOWN,
    MORE_THAT_ONE_EXPECTED,
    IDENTIFIER_EXPECTED
}

fun String.parseErrorMessages(): Set<ParseError> = lineSequence()
    .filter { it.isNotEmpty() }
    .map {
        when (it) {
            "not a statement" -> ParseError.NOT_A_STATEMENT
            "';' expected" -> ParseError.SEMICOLON_EXPECTED
            "'(' expected" -> ParseError.OPEN_BRACKET_EXPECTED
            "-> expected" -> ParseError.ARROW_EXPECTED
            else -> ParseError.UNKNOWN
        }
    }.toSet()

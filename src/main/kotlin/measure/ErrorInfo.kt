package measure

import kotlinx.serialization.Serializable

@Serializable
data class ErrorInfo(val type: ParseError, var msg: String? = null, var line: Long? = null, var col: Long? = null)
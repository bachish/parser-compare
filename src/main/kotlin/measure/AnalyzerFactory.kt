package measure

object AnalyzerFactory {
    fun create(analyzerType: MeasureType): IMeasurer {
        return when (analyzerType) {
            MeasureType.ParsingTime -> ParsingTimeMeasurer()
            MeasureType.Errors -> ErrorCollector()
            MeasureType.Similarity -> TODO()
        }
    }

    fun create(analyzerTypeView: String): IMeasurer {
        val analyzerType = runCatching { MeasureType.valueOf(analyzerTypeView) }.getOrElse {
            throw IllegalArgumentException("Unknown analyzer type: $analyzerTypeView")
        }
        return create(analyzerType)
    }
}

enum class MeasureType {
    ParsingTime, Errors, Similarity,
}

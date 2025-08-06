package measure

import parsers.IRecoveryAnalyzer

interface IMeasurer {
    fun <T, N> measure(analyzer: IRecoveryAnalyzer<T, N>, inputDirectory: String, outputCsvFile: String)
}
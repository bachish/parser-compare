package measure

import parsers.IRecoveryAnalyzer

interface IMeasurer {
    fun <T> measure(analyzer: IRecoveryAnalyzer<T>, inputDirectory: String, outputCsvFile: String)
}
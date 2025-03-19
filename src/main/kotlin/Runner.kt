import java.io.File

//import CodeAnalyzer
fun main(args: Array<String>) {
    val language = "java8"
    val analyzer = OldCodeAnalyzer(language)

    val filePath = args[0]
//    val filePath = "C:\\data\\java_src_files\\49030395_1874025707"
    val file = File(filePath)
    val s = analyzer.parseFile(file)
}

//fun main() {
//    println("Начинаем потреблять память...")
//
//    // Максимальный лимит памяти в мегабайтах (например, 500 MB)
//    val maxMemoryLimitMB = 500
//
//    // Размер одного объекта в мегабайтах
//    val objectSizeMB = 10
//
//    // Создаем список для хранения больших объектов
//    val memoryHog = mutableListOf<ByteArray>()
//
//    var allocatedMemoryMB = 0
//    while (allocatedMemoryMB < maxMemoryLimitMB) {
//        // Добавляем массив байтов размером objectSizeMB МБ
//        val largeObject = ByteArray(objectSizeMB * 1024 * 1024) // objectSizeMB MB
//        memoryHog.add(largeObject)
//
//        allocatedMemoryMB += objectSizeMB
//        println("Выделено: $allocatedMemoryMB MB")
//    }
//
//}
//

class ErrorsUtils {
}

var missingSemicolon = "class Main {int x = 12}"
var correctCode = "class Main {int x = 12;}"
var missingArrow = "class Main { Runnable r = () System.out.print(\"Run method\");}"
var missingOpenBracket = "class Main }"
var notAStatement = "class Main {int foo() {42;}}"
var identifierExpected = "class Main {int foo() {int;}}"
var todo = "class Main() {int x}" //unexpected token??

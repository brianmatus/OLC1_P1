java -jar lib/jflex-full-1.7.0.jar src/com/matus/analyzers/lexic_analyzer.flex
cd src\com\matus\analyzers
java -jar ..\..\..\..\lib\java-cup-11b.jar -parser SyntacticAnalyzer -symbols Symbols syntactic_analyser.cup
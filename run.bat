@echo off
set JAVA_HOME=C:\Users\PC FACTOR BLACK\.jdks\ms-21.0.8
set PATH=%JAVA_HOME%\bin;%PATH%
echo Using Java: %JAVA_HOME%
java -version
mvn spring-boot:run -Dspring-boot.run.profiles=local

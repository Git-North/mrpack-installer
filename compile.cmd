for /f %%A in ('powershell -Command "(Invoke-WebRequest -Uri 'https://mvnrepository.com/artifact/org.json/json' -UseBasicParsing).Content -match 'Latest Version:\s*<a[^>]*>(.*?)</a>';$matches[1]"') do set "LATEST_VERSION=%%A"
echo Latest json.jar version: %LATEST_VERSION%
curl https://repo1.maven.org/maven2/org/json/json/%LATEST_VERSION%/json-%LATEST_VERSION%.jar -o json.jar
echo Download complete.
javac -cp "%~dp0json.jar;." mrpack_util.java
pause

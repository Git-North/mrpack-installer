for /f "delims=" %%a in ('powershell -command "(Invoke-WebRequest 'https://repo1.maven.org/maven2/org/json/json/maven-metadata.xml').Content | Select-String -Pattern '<latest>(.*?)</latest>' | ForEach-Object { $_.Matches.Groups[1].Value }"') do set latest=%%a
echo Latest json.jar version: %latest%
curl https://repo1.maven.org/maven2/org/json/json/%latest%/json-%latest%.jar -o json.jar
echo Download complete.
javac -cp "%~dp0json.jar;." mrpack_util.java
pause

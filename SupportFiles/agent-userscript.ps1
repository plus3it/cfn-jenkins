Param(
  [String]$JenkinsMaster
  ,
  [String]$WorkDir
  ,
  [String]$SecretKey
  ,
  [String]$JNLPUrl
  ,
  [String]$JavaBucketPath
  ,
  [String]$WinSwBucketPath
)

$ErrorActionPreference = "Stop"

function downloadJNLP($JenkinsMaster, $WorkDir) {
    New-Item -ItemType directory -Path $WorkDir -Force
    $url = "https://$JenkinsMaster/jnlpJars/agent.jar"
    $output_file = "$Workdir\agent.jar"
    Invoke-WebRequest -Uri "$url" -OutFile "$output_file"
}

function InstallJava($bucketname,$objectname) {
    Copy-S3Object -BucketName $bucketname -Key $objectname -LocalFile c:\cfn\scripts\java_install.exe;
    c:\cfn\scripts\java_install.exe /s INSTALL_SILENT=1 STATIC=0 AUTO_UPDATE=0 WEB_JAVA=1 WEB_JAVA_SECURITY_LEVEL=H WEB_ANALYTICS=0 EULA=0 REBOOT=0 NOSTARTMENU=0 SPONSORS=0 ;
}

function createJenkinsXML ($JNLPUrl, $secret, $workDir) {
    $jenkxmlfile = "$workDir\jenkagent.xml"
    "<service>" | Out-File -Encoding Ascii -append $jenkxmlfile
    "    <id>JenkinsAgent</id>" | Out-File -Encoding Ascii -append $jenkxmlfile
    "    <name>JenkinsAgent</name>" | Out-File -Encoding Ascii -append $jenkxmlfile
    "    <description>JenkinsAgent</description>" | Out-File -Encoding Ascii -append $jenkxmlfile
    "    <env name=`"JENKINS_HOME`" value=`"$workDir`"/>" | Out-File -Encoding Ascii -append $jenkxmlfile
    "    <executable>java</executable>" | Out-File -Encoding ascii -Append $jenkxmlfile
    "    <arguments>-Xrs -Xmx256m -jar `"$workDir\agent.jar`" -jnlpUrl $JNLPUrl -secret $secret -workDir `"$workDir`"</arguments>" | Out-File -Encoding ascii -Append $jenkxmlfile
    "    <logmode>rotate</logmode>" | Out-File -Encoding ascii -Append $jenkxmlfile
    "</service>" | Out-File -Encoding ascii -Append $jenkxmlfile
}

function setupServiceWrapper($bucketname,$objectname,$workDir) {
    Copy-S3Object -BucketName $bucketname -Key $objectname -LocalFile "$workDir"\jenkagent.exe
    $cmd = "$workDir\jenkagent.exe install"
    &cmd
}

downloadJNLP $JenkinsMaster $WorkDir 

createJenkinsXML $JNLPUrl $SecretKey $WorkDir

$extra,$mystring = $JavaBucketPath -split("https://s3.[0-9A-za-z.-]+/", 2)
$bucketname,$objectname = $mystring -split ("/", 2)
InstallJava $bucketname $objectname

$extra,$mystring = $WinSwBucketPath -split("https://s3.[0-9A-za-z.-]+/", 2)
$bucketname,$objectname = $mystring -split ("/", 2)
setupService $bucketname $objectname

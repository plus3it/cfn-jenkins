## Deploying Jenkins Master Server Template Using Only AWS Native Tools

This document describes how to launch the Jenkins Master Server templates using only native AWS tools


### AWS CLI

The AWS CLI can be used to launch the template using either a locally-staged copy of the template or an S3-hosted copy of the template. In either case it will be necessary to supply template-parameters. While this can be done directly within the CLI, readability and repeatability are aided by using a parameters file. For the standalone template, the parameters file will resemble the following:

~~~~
[
    {
        "ParameterKey": "AdminPubkeyURL",
        "ParameterValue": "https://s3.amazonaws.com/common-tools/AdminPubkeys.txt"
    },
    {
        "ParameterKey": "AmiDistro",
        "ParameterValue": "CentOS"
    },
    {
        "ParameterKey": "AmiId",
        "ParameterValue": "ami-0394fe9914b475c53"
    },
    {
        "ParameterKey": "AppVolumeDevice",
        "ParameterValue": "/dev/xvdf"
    },
    {
        "ParameterKey": "AppVolumeMountPath",
        "ParameterValue": "/var/lib/jenkins"
    },
    {
        "ParameterKey": "AppVolumeSize",
        "ParameterValue": "30"
    },
    {
        "ParameterKey": "AppVolumeType",
        "ParameterValue": "gp2"
    },
    {
        "ParameterKey": "BackupBucket",
        "ParameterValue": "jenkins-backups"
    },
    {
        "ParameterKey": "BackupCronURL",
        "ParameterValue": "https://s3.amazonaws.com/common-tools/jenkins_backups.cron"
    },
    {
        "ParameterKey": "BackupFolder",
        "ParameterValue": "Backups"
    },
    {
        "ParameterKey": "CfnEndpointUrl",
        "ParameterValue": "https://cloudformation.us-east-1.amazonaws.com"
    },
    {
        "ParameterKey": "DnsSuffix",
        "ParameterValue": "mydev.net"
    },
    {
        "ParameterKey": "EpelRepo",
        "ParameterValue": "epel"
    },
    {
        "ParameterKey": "InstanceRole",
        "ParameterValue": "Jenkins1ICSNOOM0A340-1G0LQ0SKLBJE1"
    },
    {
        "ParameterKey": "InstanceType",
        "ParameterValue": "m4.large"
    },
    {
        "ParameterKey": "JenkinsAppinstallScriptUrl",
        "ParameterValue": "https://s3.amazonaws.com/common-tools/jenkins_appinstall.sh"
    },
    {
        "ParameterKey": "JenkinsOsPrepScriptUrl",
        "ParameterValue": "https://s3.amazonaws.com/common-tools/jenkins_osprep.sh"
    },
    {
        "ParameterKey": "JenkinsRepoKeyURL",
        "ParameterValue": "https://pkg.jenkins.io/redhat-stable/jenkins.io.key"
    },
    {
        "ParameterKey": "JenkinsRepoURL",
        "ParameterValue": "http://pkg.jenkins.io/redhat-stable"
    },
    {
        "ParameterKey": "JenkinsRpmName",
        "ParameterValue": "jenkins-2.107.2"
    },
    {
        "ParameterKey": "KeyPairName",
        "ParameterValue": "mykey-dev"
    },
    {
        "ParameterKey": "NoPublicIp",
        "ParameterValue": "true"
    },
    {
        "ParameterKey": "NoReboot",
        "ParameterValue": "true"
    },
    {
        "ParameterKey": "NoUpdates",
        "ParameterValue": "false"
    },
    {
        "ParameterKey": "PipIndexFips",
        "ParameterValue": "https://pypi.org/simple/"
    },
    {
        "ParameterKey": "PipRpm",
        "ParameterValue": "python2-pip"
    },
    {
        "ParameterKey": "ProvisionUser",
        "ParameterValue": "builder"
    },
    {
        "ParameterKey": "PyStache",
        "ParameterValue": "pystache"
    },
    {
        "ParameterKey": "SecurityGroupIds",
        "ParameterValue": "sg-0272486cf11b0d72a"
    },
    {
        "ParameterKey": "ServerHostname",
        "ParameterValue": "jenkins-master"
    },
    {
        "ParameterKey": "SubnetIds",
        "ParameterValue": "subnet-0a1e486b"
    },
    {
        "ParameterKey": "WatchmakerConfig",
        "ParameterValue": ""
    },
    {
        "ParameterKey": "WatchmakerEnvironment",
        "ParameterValue": ""
    }
]
~~~~

Done directly within the cli, the required parameters-enumeration would look like:

~~~~
--parameters '[{"ParameterKey":"AdminPubkeyURL","ParameterValue":"https://s3.amazonaws.com/common-tools/AdminPubkeys.txt"},{"ParameterKey":"AmiDistro","ParameterValue":"CentOS"},{"ParameterKey":"AmiId","ParameterValue":"ami-0394fe9914b475c53"},{"ParameterKey":"AppVolumeDevice","ParameterValue":"/dev/xvdf"},{"ParameterKey":"AppVolumeMountPath","ParameterValue":"/var/lib/jenkins"},{"ParameterKey":"AppVolumeSize","ParameterValue":"30"},{"ParameterKey":"AppVolumeType","ParameterValue":"gp2"},{"ParameterKey":"BackupBucket","ParameterValue":"jenkins-backups"},{"ParameterKey":"BackupCronURL","ParameterValue":"https://s3.amazonaws.com/common-tools/jenkins_backups.cron"},{"ParameterKey":"BackupFolder","ParameterValue":"Backups"},{"ParameterKey":"CfnEndpointUrl","ParameterValue":"https://cloudformation.us-east-1.amazonaws.com"},{"ParameterKey":"DnsSuffix","ParameterValue":"mydev.net"},{"ParameterKey":"EpelRepo","ParameterValue":"epel"},{"ParameterKey":"InstanceRole","ParameterValue":"Jenkins1ICSNOOM0A340-1G0LQ0SKLBJE1"},{"ParameterKey":"InstanceType","ParameterValue":"m4.large"},{"ParameterKey":"JenkinsAppinstallScriptUrl","ParameterValue":"https://s3.amazonaws.com/common-tools/jenkins_appinstall.sh"},{"ParameterKey":"JenkinsOsPrepScriptUrl","ParameterValue":"https://s3.amazonaws.com/common-tools/jenkins_osprep.sh"},{"ParameterKey":"JenkinsRepoKeyURL","ParameterValue":"https://pkg.jenkins.io/redhat-stable/jenkins.io.key"},{"ParameterKey":"JenkinsRepoURL","ParameterValue":"http://pkg.jenkins.io/redhat-stable"},{"ParameterKey":"JenkinsRpmName","ParameterValue":"jenkins-2.107.2"},{"ParameterKey":"KeyPairName","ParameterValue":"mykey-dev"},{"ParameterKey":"NoPublicIp","ParameterValue":"true"},{"ParameterKey":"NoReboot","ParameterValue":"true"},{"ParameterKey":"NoUpdates","ParameterValue":"false"},{"ParameterKey":"PipIndexFips","ParameterValue":"https://pypi.org/simple/"},{"ParameterKey":"PipRpm","ParameterValue":"python2-pip"},{"ParameterKey":"ProvisionUser","ParameterValue":"builder"},{"ParameterKey":"PyStache","ParameterValue":"pystache"},{"ParameterKey":"SecurityGroupIds","ParameterValue":"sg-0272486cf11b0d72a"},{"ParameterKey":"ServerHostname","ParameterValue":"jenkins-master"},{"ParameterKey":"SubnetIds","ParameterValue":"subnet-0a1e486b"},{"ParameterKey":"WatchmakerConfig","ParameterValue":""},{"ParameterKey":"WatchmakerEnvironment","ParameterValue":""}]'
~~~~

For the autoscale template, the parameters file will resemble the following:

~~~~
[
    {
        "ParameterKey": "AdminPubkeyURL",
        "ParameterValue": "https://s3.amazonaws.com/common-tools/AdminPubkeys.txt"
    },
    {
        "ParameterKey": "AmiId",
        "ParameterValue": "ami-0394fe9914b475c53"
    },
    {
        "ParameterKey": "AppVolumeDevice",
        "ParameterValue": "true"
    },
    {
        "ParameterKey": "AppVolumeMountPath",
        "ParameterValue": "/var/lib/jenkins"
    },
    {
        "ParameterKey": "AppVolumeSize",
        "ParameterValue": "20"
    },
    {
        "ParameterKey": "AppVolumeType",
        "ParameterValue": "gp2"
    },
    {
        "ParameterKey": "BackupBucket",
        "ParameterValue": "jenkins-backups"
    },
    {
        "ParameterKey": "BackupFolder",
        "ParameterValue": "Backups"
    },
    {
        "ParameterKey": "CfnBootstrapUtilsUrl",
        "ParameterValue": "https://s3.amazonaws.com/cloudformation-examples/aws-cfn-bootstrap-latest.tar.gz"
    },
    {
        "ParameterKey": "CfnEndpointUrl",
        "ParameterValue": "https://cloudformation.us-east-1.amazonaws.com"
    },
    {
        "ParameterKey": "CfnGetPipUrl",
        "ParameterValue": "https://bootstrap.pypa.io/2.6/get-pip.py"
    },
    {
        "ParameterKey": "CloudWatchAgentUrl",
        "ParameterValue": "s3://amazoncloudwatch-agent/linux/amd64/latest/AmazonCloudWatchAgent.zip"
    },
    {
        "ParameterKey": "DesiredCapacity",
        "ParameterValue": "1"
    },
    {
        "ParameterKey": "EpelRepo",
        "ParameterValue": "epel"
    },
    {
        "ParameterKey": "InstanceRoleName",
        "ParameterValue": "INSTANCE-Jenkins"
    },
    {
        "ParameterKey": "InstanceRoleProfile",
        "ParameterValue": "Jenkins1ICSNOOM0A340-1G0LQ0SKLBJE1"
    },
    {
        "ParameterKey": "InstanceType",
        "ParameterValue": "m5.xlarge"
    },
    {
        "ParameterKey": "JenkinsAppinstallScriptUrl",
        "ParameterValue": "https://common-tools.s3.amazonaws.com/jenkins_appinstall.sh"
    },
    {
        "ParameterKey": "JenkinsOsPrepScriptUrl",
        "ParameterValue": "https://common-tools.s3.amazonaws.com/jenkins_osprep.sh"
    },
    {
        "ParameterKey": "JenkinsRepoKeyURL",
        "ParameterValue": "https://pkg.jenkins.io/redhat-stable/jenkins.io.key"
    },
    {
        "ParameterKey": "JenkinsRepoURL",
        "ParameterValue": "http://pkg.jenkins.io/redhat-stable"
    },
    {
        "ParameterKey": "JenkinsRpmName",
        "ParameterValue": "jenkins-2.107.2"
    },
    {
        "ParameterKey": "KeyPairName",
        "ParameterValue": ""
    },
    {
        "ParameterKey": "LoadBalancerNames",
        "ParameterValue": "jenkins-1V1AHQPHQ2B0B"
    },
    {
        "ParameterKey": "MaxCapacity",
        "ParameterValue": "1"
    },
    {
        "ParameterKey": "MinCapacity",
        "ParameterValue": "0"
    },
    {
        "ParameterKey": "NoPublicIp",
        "ParameterValue": "true"
    },
    {
        "ParameterKey": "NoReboot",
        "ParameterValue": "false"
    },
    {
        "ParameterKey": "NoUpdates",
        "ParameterValue": "false"
    },
    {
        "ParameterKey": "ProvisionUser",
        "ParameterValue": "ec2-user"
    },
    {
        "ParameterKey": "PypiIndexUrl",
        "ParameterValue": "https://pypi.org/simple"
    },
    {
        "ParameterKey": "RootVolumeSize",
        "ParameterValue": "20"
    },
    {
        "ParameterKey": "ScaleDownSchedule",
        "ParameterValue": "30 0 * * *"
    },
    {
        "ParameterKey": "ScaleUpSchedule",
        "ParameterValue": "0 10 * * *"
    },
    {
        "ParameterKey": "SecurityGroupIds",
        "ParameterValue": "sg-0272486cf11b0d72a,sg-fe08d113"
    },
    {
        "ParameterKey": "SubnetIds",
        "ParameterValue": "subnet-0a1e486b,subnet-24aff602,subnet-448318a1"
    },
    {
        "ParameterKey": "TargetGroupArns",
        "ParameterValue": ""
    },
    {
        "ParameterKey": "ToggleCfnInitUpdate",
        "ParameterValue": "A"
    },
    {
        "ParameterKey": "ToggleNewInstances",
        "ParameterValue": "A"
    },
    {
        "ParameterKey": "WatchmakerAdminGroups",
        "ParameterValue": ""
    },
    {
        "ParameterKey": "WatchmakerAdminUsers",
        "ParameterValue": ""
    },
    {
        "ParameterKey": "WatchmakerComputerName",
        "ParameterValue": "jenkins-master.mydev.net"
    },
    {
        "ParameterKey": "WatchmakerConfig",
        "ParameterValue": ""
    },
    {
        "ParameterKey": "WatchmakerEnvironment",
        "ParameterValue": "dev"
    },
    {
        "ParameterKey": "WatchmakerOuPath",
        "ParameterValue": ""
    }
]
~~~~

Done directly within the cli, the required parameters-enumeration would look like:

~~~~
--parameters '[{"ParameterKey":"AdminPubkeyURL","ParameterValue":"https://s3.amazonaws.com/common-tools/AdminPubkeys.txt"},{"ParameterKey":"AmiId","ParameterValue":"ami-0394fe9914b475c53"},{"ParameterKey":"AppVolumeDevice","ParameterValue":"true"},{"ParameterKey":"AppVolumeMountPath","ParameterValue":"/var/lib/jenkins"},{"ParameterKey":"AppVolumeSize","ParameterValue":"20"},{"ParameterKey":"AppVolumeType","ParameterValue":"gp2"},{"ParameterKey":"BackupBucket","ParameterValue":"jenkins-backups"},{"ParameterKey":"BackupFolder","ParameterValue":"Backups"},{"ParameterKey":"CfnBootstrapUtilsUrl","ParameterValue":"https://s3.amazonaws.com/cloudformation-examples/aws-cfn-bootstrap-latest.tar.gz"},{"ParameterKey":"CfnEndpointUrl","ParameterValue":"https://cloudformation.us-east-1.amazonaws.com"},{"ParameterKey":"CfnGetPipUrl","ParameterValue":"https://bootstrap.pypa.io/2.6/get-pip.py"},{"ParameterKey":"CloudWatchAgentUrl","ParameterValue":"s3://amazoncloudwatch-agent/linux/amd64/latest/AmazonCloudWatchAgent.zip"},{"ParameterKey":"DesiredCapacity","ParameterValue":"1"},{"ParameterKey":"EpelRepo","ParameterValue":"epel"},{"ParameterKey":"InstanceRoleName","ParameterValue":"INSTANCE-Jenkins"},{"ParameterKey":"InstanceRoleProfile","ParameterValue":"Jenkins-C1LZCAQNDFZ8-B3QQFA"},{"ParameterKey":"InstanceType","ParameterValue":"m5.xlarge"},{"ParameterKey":"JenkinsAppinstallScriptUrl","ParameterValue":"https://common-tools.s3.amazonaws.com/jenkins_appinstall.sh"},{"ParameterKey":"JenkinsOsPrepScriptUrl","ParameterValue":"https://common-tools.s3.amazonaws.com/jenkins_osprep.sh"},{"ParameterKey":"JenkinsRepoKeyURL","ParameterValue":"https://pkg.jenkins.io/redhat-stable/jenkins.io.key"},{"ParameterKey":"JenkinsRepoURL","ParameterValue":"http://pkg.jenkins.io/redhat-stable"},{"ParameterKey":"JenkinsRpmName","ParameterValue":"jenkins-2.107.2"},{"ParameterKey":"KeyPairName","ParameterValue":""},{"ParameterKey":"LoadBalancerNames","ParameterValue":"jenkins-1V1AHQPHQ2B0B"},{"ParameterKey":"MaxCapacity","ParameterValue":"1"},{"ParameterKey":"MinCapacity","ParameterValue":"0"},{"ParameterKey":"NoPublicIp","ParameterValue":"true"},{"ParameterKey":"NoReboot","ParameterValue":"false"},{"ParameterKey":"NoUpdates","ParameterValue":"false"},{"ParameterKey":"ProvisionUser","ParameterValue":"ec2-user"},{"ParameterKey":"PypiIndexUrl","ParameterValue":"https://pypi.org/simple"},{"ParameterKey":"RootVolumeSize","ParameterValue":"20"},{"ParameterKey":"ScaleDownSchedule","ParameterValue":"30 0 * * *"},{"ParameterKey":"ScaleUpSchedule","ParameterValue":"0 10 * * *"},{"ParameterKey":"SecurityGroupIds","ParameterValue":"sg-0272486cf11b0d72a,sg-fe08d113"},{"ParameterKey":"SubnetIds","ParameterValue":"subnet-0a1e486b,subnet-24aff602,subnet-448318a1"},{"ParameterKey":"TargetGroupArns","ParameterValue":""},{"ParameterKey":"ToggleCfnInitUpdate","ParameterValue":"A"},{"ParameterKey":"ToggleNewInstances","ParameterValue":"A"},{"ParameterKey":"WatchmakerAdminGroups","ParameterValue":""},{"ParameterKey":"WatchmakerAdminUsers","ParameterValue":""},{"ParameterKey":"WatchmakerComputerName","ParameterValue":"jenkins-master.mydev.net"},{"ParameterKey":"WatchmakerConfig","ParameterValue":""},{"ParameterKey":"WatchmakerEnvironment","ParameterValue":"dev"},{"ParameterKey":"WatchmakerOuPath","ParameterValue":""}]'
~~~~

With the highly-parameterized nature of the Jenkins Master Server templates, specifying parameter-values directly within the CLI becomes practically impossible.

The following sub-sections assume:
- The template user has previously used `aws configure` to set a default region for their commands to run within. If this is not the case, it will be necessary to insert the `--region <REGION_NAME>` option to the CLI command
- The template user is not attempting to execute the command against a non-default account-profile. If this is not the case, it will be necessary to insert the `--profile <PROFILE_NAME>` option to the CLI command

#### Locally Hosted Template

When referencing a locally-hosted file, one uses the `--template-body` parameter. Depending on the OS type being launched from specifying the path to the template may require using the `file://` URI. Similarly, if using a parameters file, one may need to use the `file://` URI. The resulting command will look like:

~~~~
aws cloudformation create-stack --stack-name make_jenkins_EC2-Master-instance.tmplt.json \
  --template-body file:///<FULLY_QUALIFIED>/<PATH>/<TO>/<TEMPLATE> \
  --parameters file:///<FULLY_QUALIFIED>/<PATH>/<TO>/<PARAMETER_FILE>
~~~~

Or:

~~~~
aws cloudformation create-stack --stack-name make_jenkins_EC2-Master-autoscale.tmplt.json \
  --template-body file://<RELATIVE>/<PATH>/<TO>/<TEMPLATE> \
  --parameters file://<RELATIVE>/<PATH>/<TO>/<PARAMETER_FILE>
~~~~

The latter saves typing if the template- and parameter-files are either co-located or proximal to each other within the local filesystem. This can mean the difference between typing:

`file:///home/myuser/GIT/cfn-jenkins/Templates/make_jenkins_EC2-Master-instance.tmplt.json`

and typing:

`file://make_jenkins_EC2-Master-instance.tmplt.json`

#### S3-Hosted Template

When referencing a S3-hosted file, one uses the `--template-url` parameter. If using a parameters file, one may need to use the `file://` URI. The resulting command will look like:

~~~~
aws cloudformation create-stack --stack-name make_jenkins_EC2-Master-autoscale.tmplt.json \
  --template-url https://s3.<REGION>.<AMAZON_TLD>/<BUCKET_NAME>/<TEMPLATE_NAME> \
  --parameters file:///<FULLY_QUALIFIED>/<PATH>/<TO>/<PARAMETER_FILE>
~~~~

Or:

~~~~
aws cloudformation create-stack --stack-name make_jenkins_EC2-Master-instance.tmplt.json \
  --template-url https://s3.<REGION>.<AMAZON_TLD>/<BUCKET_NAME>/<TEMPLATE_NAME> \
  --parameters file://<RELATIVE>/<PATH>/<TO>/<PARAMETER_FILE>
~~~~

### AWS Web UI

(TBD)


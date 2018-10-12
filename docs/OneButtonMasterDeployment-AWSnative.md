## "One-button", Templated Deployment of Jenkins Service and Dependencies Using Only AWS Native Tools

This document describes how to launch an entire Jenkins-stack via a "one button" CFN template  using only native AWS tools

### AWS CLI

The AWS CLI can be used to launch the template using either a locally-staged copy of the template or an S3-hosted copy of the template. In either case it will be necessary to supply template-parameters. While this can be done directly within the CLI, readability and repeatability are aided by using a parameters file.

Used with the instance "parent" template, the parameters file will resemble the following:

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
        "ParameterValue": "ami-02bcab782224ad2ae"
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
        "ParameterValue": "25"
    },
    {
        "ParameterKey": "AppVolumeType",
        "ParameterValue": "gp2"
    },
    {
        "ParameterKey": "BackendTimeout",
        "ParameterValue": "600"
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
        "ParameterKey": "BucketTemplate",
        "ParameterValue": "https://s3.amazonaws.com/common-tools/make_jenkins_S3-bucket.tmplt.json"
    },
    {
        "ParameterKey": "DnsSuffix",
        "ParameterValue": "mydev.net"
    },
    {
        "ParameterKey": "Ec2Template",
        "ParameterValue": "https://s3.amazonaws.com/common-tools/make_jenkins_EC2-instance.tmplt.json"
    },
    {
        "ParameterKey": "ElbTemplate",
        "ParameterValue": "https://s3.amazonaws.com/common-tools/make_jenkins_ELBv1-pub.tmplt.json"
    },
    {
        "ParameterKey": "EpelRepo",
        "ParameterValue": "epel"
    },
    {
        "ParameterKey": "HaSubnets",
        "ParameterValue": "subnet-0e9c98e122846e3,subnet-aeafba1f287e29c,subnet-6b6896667ca92dd"
    },
    {
        "ParameterKey": "IamRoleTemplate",
        "ParameterValue": "https://s3.amazonaws.com/common-tools/make_jenkins_IAM-role.tmplt.json"
    },
    {
        "ParameterKey": "InstanceType",
        "ParameterValue": "m4.large"
    },
    {
        "ParameterKey": "JenkinsAgentPort",
        "ParameterValue": "45158"
    },
    {
        "ParameterKey": "JenkinsAppinstallScriptUrl",
        "ParameterValue": "https://s3.amazonaws.com/common-tools/jenkins_appinstall.sh"
    },
    {
        "ParameterKey": "JenkinsBackupBucket",
        "ParameterValue": ""
    },
    {
        "ParameterKey": "JenkinsListenerCert",
        "ParameterValue": "63c8a276-de0e-095c-c743-2899f808"
    },
    {
        "ParameterKey": "JenkinsListenPort",
        "ParameterValue": "443"
    },
    {
        "ParameterKey": "JenkinsOsPrepScriptUrl",
        "ParameterValue": "https://s3.amazonaws.com/common-tools/jenkins_osprep.sh"
    },
    {
        "ParameterKey": "JenkinsPassesSsh",
        "ParameterValue": "false"
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
        "ParameterValue": "jenkins"
    },
    {
        "ParameterKey": "JenkinsServicePort",
        "ParameterValue": "8080"
    },
    {
        "ParameterKey": "KeyPairName",
        "ParameterValue": "deploy-key-dev"
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
        "ParameterValue": "true"
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
        "ParameterKey": "ProxyPrettyName",
        "ParameterValue": ""
    },
    {
        "ParameterKey": "PyStache",
        "ParameterValue": "pystache"
    },
    {
        "ParameterKey": "RolePrefix",
        "ParameterValue": ""
    },
    {
        "ParameterKey": "SecurityGroupTemplate",
        "ParameterValue": "https://s3.amazonaws.com/common-tools/make_jenkins_SGs.tmplt.json"
    },
    {
        "ParameterKey": "ServerHostname",
        "ParameterValue": "jenkins-master"
    },
    {
        "ParameterKey": "ServiceTld",
        "ParameterValue": "amazonaws.com"
    },
    {
        "ParameterKey": "SubnetId",
        "ParameterValue": "subnet-483ec301"
    },
    {
        "ParameterKey": "TargetVPC",
        "ParameterValue": "vpc-a082aa67"
    }
]
~~~~

Done directly within the cli, the required parameters-enumeration for the "parent" template would look like:

~~~~
[{"ParameterKey":"AdminPubkeyURL","ParameterValue":"https://s3.amazonaws.com/common-tools/AdminPubkeys.txt"},{"ParameterKey":"AmiDistro","ParameterValue":"CentOS"},{"ParameterKey":"AmiId","ParameterValue":"ami-02bcab782224ad2ae"},{"ParameterKey":"AppVolumeDevice","ParameterValue":"/dev/xvdf"},{"ParameterKey":"AppVolumeMountPath","ParameterValue":"/var/lib/jenkins"},{"ParameterKey":"AppVolumeSize","ParameterValue":"25"},{"ParameterKey":"AppVolumeType","ParameterValue":"gp2"},{"ParameterKey":"BackendTimeout","ParameterValue":"600"},{"ParameterKey":"BackupCronURL","ParameterValue":"https://s3.amazonaws.com/common-tools/jenkins_backups.cron"},{"ParameterKey":"BackupFolder","ParameterValue":"Backups"},{"ParameterKey":"BucketTemplate","ParameterValue":"https://s3.amazonaws.com/common-tools/make_jenkins_S3-bucket.tmplt.json"},{"ParameterKey":"DnsSuffix","ParameterValue":"mydev.net"},{"ParameterKey":"Ec2Template","ParameterValue":"https://s3.amazonaws.com/common-tools/make_jenkins_EC2-instance.tmplt.json"},{"ParameterKey":"ElbTemplate","ParameterValue":"https://s3.amazonaws.com/common-tools/make_jenkins_ELBv1-pub.tmplt.json"},{"ParameterKey":"EpelRepo","ParameterValue":"epel"},{"ParameterKey":"HaSubnets","ParameterValue":"subnet-0e9c98e122846e3,subnet-aeafba1f287e29c,subnet-6b6896667ca92dd"},{"ParameterKey":"IamRoleTemplate","ParameterValue":"https://s3.amazonaws.com/common-tools/make_jenkins_IAM-role.tmplt.json"},{"ParameterKey":"InstanceType","ParameterValue":"m4.large"},{"ParameterKey":"JenkinsAgentPort","ParameterValue":"45158"},{"ParameterKey":"JenkinsAppinstallScriptUrl","ParameterValue":"https://s3.amazonaws.com/common-tools/jenkins_appinstall.sh"},{"ParameterKey":"JenkinsBackupBucket","ParameterValue":""},{"ParameterKey":"JenkinsListenerCert","ParameterValue":"63c8a276-de0e-095c-c743-2899f808"},{"ParameterKey":"JenkinsListenPort","ParameterValue":"443"},{"ParameterKey":"JenkinsOsPrepScriptUrl","ParameterValue":"https://s3.amazonaws.com/common-tools/jenkins_osprep.sh"},{"ParameterKey":"JenkinsPassesSsh","ParameterValue":"false"},{"ParameterKey":"JenkinsRepoKeyURL","ParameterValue":"https://pkg.jenkins.io/redhat-stable/jenkins.io.key"},{"ParameterKey":"JenkinsRepoURL","ParameterValue":"http://pkg.jenkins.io/redhat-stable"},{"ParameterKey":"JenkinsRpmName","ParameterValue":"jenkins"},{"ParameterKey":"JenkinsServicePort","ParameterValue":"8080"},{"ParameterKey":"KeyPairName","ParameterValue":"deploy-key-dev"},{"ParameterKey":"NoPublicIp","ParameterValue":"true"},{"ParameterKey":"NoReboot","ParameterValue":"true"},{"ParameterKey":"NoUpdates","ParameterValue":"true"},{"ParameterKey":"PipIndexFips","ParameterValue":"https://pypi.org/simple/"},{"ParameterKey":"PipRpm","ParameterValue":"python2-pip"},{"ParameterKey":"ProvisionUser","ParameterValue":"builder"},{"ParameterKey":"ProxyPrettyName","ParameterValue":""},{"ParameterKey":"PyStache","ParameterValue":"pystache"},{"ParameterKey":"RolePrefix","ParameterValue":""},{"ParameterKey":"SecurityGroupTemplate","ParameterValue":"https://s3.amazonaws.com/common-tools/make_jenkins_SGs.tmplt.json"},{"ParameterKey":"ServerHostname","ParameterValue":"jenkins-master"},{"ParameterKey":"ServiceTld","ParameterValue":"amazonaws.com"},{"ParameterKey":"SubnetId","ParameterValue":"subnet-483ec301"},{"ParameterKey":"TargetVPC","ParameterValue":"vpc-a082aa67"}]
~~~~

With the highly-parameterized nature of the "parent" template, specifying parameter-values directly within the CLI becomes practically impossible.

*Note:* The "parent" template references a group of child templates. The "parent" template is used to coordinate the running of these child templates. Parameters fed to the "parent" are, in turn, fed to each of the child templates that needs a given parameter. This accounts for the extremely large number of parameters to pass in. Further, all of the child templates _must_ be hosted in S3.

The following sub-sections assume:

The template user has previously used aws configure to set a default region for their commands to run within. If this is not the case, it will be necessary to insert the --region <REGION_NAME> option to the CLI command
The template user is not attempting to execute the command against a non-default account-profile. If this is not the case, it will be necessary to insert the --profile <PROFILE_NAME> option to the CLI command

#### Locally Hosted Template

When referencing a locally-hosted file, one uses the `--template-body` parameter. Depending on the OS type being launched from specifying the path to the template may require using the `file://` URI. Similarly, if using a parameters file, one may need to use the `file://` URI. The resulting command will look like:

~~~~
aws cloudformation create-stack --stack-name make_jenkins_parent-instance.tmplt.json \
  --template-body file:///<FULLY_QUALIFIED>/<PATH>/<TO>/<TEMPLATE> \
  --parameters file:///<FULLY_QUALIFIED>/<PATH>/<TO>/<PARAMETER_FILE>
~~~~

Or:

~~~~
aws cloudformation create-stack --stack-name make_jenkins_EC2-Agent-Linux-autoscale.tmplt.json \
  --template-body file://<RELATIVE>/<PATH>/<TO>/<TEMPLATE> \
  --parameters file://<RELATIVE>/<PATH>/<TO>/<PARAMETER_FILE>
~~~~

The latter saves typing if the template- and parameter-files are either co-located or proximal to each other within the local filesystem. This can mean the difference between typing:

`file:///home/myuser/GIT/cfn-jenkins/Templates/make_jenkins_parent-instance.tmplt.json`

and typing:

`file://make_jenkins_parent-instance.tmplt.json`

#### S3-Hosted Template

When referencing a S3-hosted file, one uses the `--template-url` parameter. If using a parameters file, one may need to use the `file://` URI. The resulting command will look like:

~~~~
aws cloudformation create-stack --stack-name make_jenkins_EC2-Agent-Linux-autoscale.tmplt.json \
  --template-url https://s3.<REGION>.<AMAZON_TLD>/<BUCKET_NAME>/<TEMPLATE_NAME> \
  --parameters file:///<FULLY_QUALIFIED>/<PATH>/<TO>/<PARAMETER_FILE>
~~~~

Or:

~~~~
aws cloudformation create-stack --stack-name make_jenkins_parent-instance.tmplt.json \
  --template-url https://s3.<REGION>.<AMAZON_TLD>/<BUCKET_NAME>/<TEMPLATE_NAME> \
  --parameters file://<RELATIVE>/<PATH>/<TO>/<PARAMETER_FILE>
~~~~

### AWS Web UI

(TBD)


## Deploying (Linux-based) Jenkins Agent-Nodes Template Using Only AWS Native Tools

This document describes how to launch Jenkins Agent-Node templates (for STIG-hardened Enterprise Linux) using only native AWS tools


### AWS CLI

The AWS CLI can be used to launch the template using either a locally-staged copy of the template or an S3-hosted copy of the template. In either case it will be necessary to supply template-parameters. While this can be done directly within the CLI, readability and repeatability are aided by using a parameters file.

Used with the standalone template, the parameters file will resemble the following:

~~~~
[
    {
        "ParameterKey": "AdminPubkeyURL",
        "ParameterValue": "https://s3.amazonaws.com/common-tools/AdminPubkeys.txt"
    },
    {
        "ParameterKey": "AgentAuthToken",
        "ParameterValue": "de37eddc6a606e8193c1c81e72b62fd97d2b3c30790d8e75524afeb9d51bab5b"
    },
    {
        "ParameterKey": "AmiId",
        "ParameterValue": "ami-0e7ca152642d2a964"
    },
    {
        "ParameterKey": "AppVolumeDevice",
        "ParameterValue": "false"
    },
    {
        "ParameterKey": "AppVolumeMountPath",
        "ParameterValue": "/var/jenkins"
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
        "ParameterKey": "CfnEndpointUrl",
        "ParameterValue": "https://cloudformation.us-east-1.amazonaws.com"
    },
    {
        "ParameterKey": "ChainScriptUrl",
        "ParameterValue": "https://s3.amazonaws.com/common-tools/chain-load.sh"
    },
    {
        "ParameterKey": "Domainname",
        "ParameterValue": "mydev.net"
    },
    {
        "ParameterKey": "EpelRepo",
        "ParameterValue": "epel"
    },
    {
        "ParameterKey": "GitRepoKeyUrl",
        "ParameterValue": ""
    },
    {
        "ParameterKey": "Hostname",
        "ParameterValue": "jenkins-agent2"
    },
    {
        "ParameterKey": "InstanceRole",
        "ParameterValue": "JenkinsTst-IamRes-1ICSNOOM0A340-InstanceProfile-1G0LQ0SKLBJE1"
    },
    {
        "ParameterKey": "InstanceType",
        "ParameterValue": "m4.large"
    },
    {
        "ParameterKey": "JenkinsAgentName",
        "ParameterValue": "Jenkins-Agent2"
    },
    {
        "ParameterKey": "JenkinsMaster",
        "ParameterValue": "jenkins.mydev.net"
    },
    {
        "ParameterKey": "JenkinsUserScriptUrl",
        "ParameterValue": "https://s3.amazonaws.com/common-tools/jenkins-agent_userscript.sh"
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
        "ParameterValue": "false"
    },
    {
        "ParameterKey": "NoUpdates",
        "ParameterValue": "false"
    },
    {
        "ParameterKey": "NpmSetupUri",
        "ParameterValue": "https://rpm.nodesource.com/setup_8.x"
    },
    {
        "ParameterKey": "PipRpm",
        "ParameterValue": "python2-pip"
    },
    {
        "ParameterKey": "PrivateIp",
        "ParameterValue": ""
    },
    {
        "ParameterKey": "ProvisionUser",
        "ParameterValue": "provision"
    },
    {
        "ParameterKey": "PypiIndexUrl",
        "ParameterValue": "https://pypi.org/simple"
    },
    {
        "ParameterKey": "PyStache",
        "ParameterValue": "pystache"
    },
    {
        "ParameterKey": "SecurityGroupIds",
        "ParameterValue": "sg-34ccf306a0dfcf56e,sg-7f3514c703cca9ba2,sg-2ad238cad41681cf6"
    },
    {
        "ParameterKey": "SubnetId",
        "ParameterValue": "subnet-b043e4af"
    },
    {
        "ParameterKey": "ZapAgentUrl",
        "ParameterValue": "https://github.com/zaproxy/zaproxy/releases/download/2.7.0/ZAP_2.7.0_Linux.tar.gz"
    }
]
~~~~

Used with the autoscale template, the parameters file will resemble the following:

~~~~
[
    {
        "ParameterKey": "AdminPubkeyURL",
        "ParameterValue": "https://s3.amazonaws.com/common-tools/AdminKeys.txt"
    },
    {
        "ParameterKey": "AgentAuthToken",
        "ParameterValue": "e51455d60a854e5f001bde5a0f5fe203aabe9f89a495f4ac02ee19ff8d3a580f"
    },
    {
        "ParameterKey": "AmiId",
        "ParameterValue": "ami-0e7ca152642d2a964"
    },
    {
        "ParameterKey": "AppVolumeDevice",
        "ParameterValue": "true"
    },
    {
        "ParameterKey": "AppVolumeMountPath",
        "ParameterValue": "/var/jenkins"
    },
    {
        "ParameterKey": "AppVolumeSize",
        "ParameterValue": "80"
    },
    {
        "ParameterKey": "AppVolumeType",
        "ParameterValue": "gp2"
    },
    {
        "ParameterKey": "CfnEndpointUrl",
        "ParameterValue": "https://cloudformation.us-east-1.amazonaws.com"
    },
    {
        "ParameterKey": "ChainScriptUrl",
        "ParameterValue": "https://s3.amazonaws.com/common-tools/chain-load.sh"
    },
    {
        "ParameterKey": "Domainname",
        "ParameterValue": "mydev.net"
    },
    {
        "ParameterKey": "EpelRepo",
        "ParameterValue": "epel"
    },
    {
        "ParameterKey": "GitRepoKeyUrl",
        "ParameterValue": ""
    },
    {
        "ParameterKey": "Hostname",
        "ParameterValue": "jenkins-agent3"
    },
    {
        "ParameterKey": "InstanceRole",
        "ParameterValue": "JenkinsTst-IamRes-1ICSNOOM0A340-InstanceProfile-1G0LQ0SKLBJE1"
    },
    {
        "ParameterKey": "InstanceType",
        "ParameterValue": "t2.medium"
    },
    {
        "ParameterKey": "JenkinsAgentName",
        "ParameterValue": "CommTest"
    },
    {
        "ParameterKey": "JenkinsMaster",
        "ParameterValue": "jenkins.mydev.net"
    },
    {
        "ParameterKey": "JenkinsUserScriptUrl",
        "ParameterValue": "https://s3.amazonaws.com/common-tools/jenkins-agent_userscript.sh"
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
        "ParameterValue": "false"
    },
    {
        "ParameterKey": "NoUpdates",
        "ParameterValue": "false"
    },
    {
        "ParameterKey": "NpmSetupUri",
        "ParameterValue": "https://rpm.nodesource.com/setup_8.x"
    },
    {
        "ParameterKey": "PipRpm",
        "ParameterValue": "python2-pip"
    },
    {
        "ParameterKey": "PrivateIp",
        "ParameterValue": ""
    },
    {
        "ParameterKey": "ProvisionUser",
        "ParameterValue": "provision"
    },
    {
        "ParameterKey": "PypiIndexUrl",
        "ParameterValue": "https://pypi.org/simple"
    },
    {
        "ParameterKey": "PyStache",
        "ParameterValue": "pystache"
    },
    {
        "ParameterKey": "SecurityGroupIds",
        "ParameterValue": "sg-c59c31fd93dc6f5c7,sg-7044673ecae45bd61"
    },
    {
        "ParameterKey": "SubnetId",
        "ParameterValue": "subnet-969c2629"
    },
    {
        "ParameterKey": "ZapAgentUrl",
        "ParameterValue": "https://github.com/zaproxy/zaproxy/releases/download/2.6.0/ZAP_2.6.0_Linux.tar.gz"
    }
]
~~~~


Done directly within the cli, the required parameters-enumeration for the standalone template would look like:

~~~~
[{"ParameterKey":"AdminPubkeyURL","ParameterValue":"https://s3.amazonaws.com/common-tools/AdminPubkeys.txt"},{"ParameterKey":"AgentAuthToken","ParameterValue":"de37eddc6a606e8193c1c81e72b62fd97d2b3c30790d8e75524afeb9d51bab5b"},{"ParameterKey":"AmiId","ParameterValue":"ami-0e7ca152642d2a964"},{"ParameterKey":"AppVolumeDevice","ParameterValue":"false"},{"ParameterKey":"AppVolumeMountPath","ParameterValue":"/var/jenkins"},{"ParameterKey":"AppVolumeSize","ParameterValue":"80"},{"ParameterKey":"AppVolumeType","ParameterValue":"gp2"},{"ParameterKey":"CfnEndpointUrl","ParameterValue":"https://cloudformation.us-east-1.amazonaws.com"},{"ParameterKey":"ChainScriptUrl","ParameterValue":"https://s3.amazonaws.com/common-tools/chain-load.sh"},{"ParameterKey":"Domainname","ParameterValue":"mydev.net"},{"ParameterKey":"EpelRepo","ParameterValue":"epel"},{"ParameterKey":"GitRepoKeyUrl","ParameterValue":""},{"ParameterKey":"Hostname","ParameterValue":"jenkins-agent2"},{"ParameterKey":"InstanceRole","ParameterValue":"JenkinsTst-IamRes-1ICSNOOM0A340-InstanceProfile-1G0LQ0SKLBJE1"},{"ParameterKey":"InstanceType","ParameterValue":"m4.large"},{"ParameterKey":"JenkinsAgentName","ParameterValue":"Jenkins-Agent2"},{"ParameterKey":"JenkinsMaster","ParameterValue":"jenkins.mydev.net"},{"ParameterKey":"JenkinsUserScriptUrl","ParameterValue":"https://s3.amazonaws.com/common-tools/jenkins-agent_userscript.sh"},{"ParameterKey":"KeyPairName","ParameterValue":"deploy-key-dev"},{"ParameterKey":"NoPublicIp","ParameterValue":"true"},{"ParameterKey":"NoReboot","ParameterValue":"false"},{"ParameterKey":"NoUpdates","ParameterValue":"false"},{"ParameterKey":"NpmSetupUri","ParameterValue":"https://rpm.nodesource.com/setup_8.x"},{"ParameterKey":"PipRpm","ParameterValue":"python2-pip"},{"ParameterKey":"PrivateIp","ParameterValue":""},{"ParameterKey":"ProvisionUser","ParameterValue":"provision"},{"ParameterKey":"PypiIndexUrl","ParameterValue":"https://pypi.org/simple"},{"ParameterKey":"PyStache","ParameterValue":"pystache"},{"ParameterKey":"SecurityGroupIds","ParameterValue":"sg-34ccf306a0dfcf56e,sg-7f3514c703cca9ba2,sg-2ad238cad41681cf6"},{"ParameterKey":"SubnetId","ParameterValue":"subnet-b043e4af"},{"ParameterKey":"ZapAgentUrl","ParameterValue":"https://github.com/zaproxy/zaproxy/releases/download/2.7.0/ZAP_2.7.0_Linux.tar.gz"}]
~~~~

Done directly within the cli, the required parameters-enumeration for the autoscaling template would look like:

~~~~
[{"ParameterKey":"AdminPubkeyURL","ParameterValue":"https://s3.amazonaws.com/common-tools/AdminKeys.txt"},{"ParameterKey":"AgentAuthToken","ParameterValue":"e51455d60a854e5f001bde5a0f5fe203aabe9f89a495f4ac02ee19ff8d3a580f"},{"ParameterKey":"AmiId","ParameterValue":"ami-0e7ca152642d2a964"},{"ParameterKey":"AppVolumeDevice","ParameterValue":"true"},{"ParameterKey":"AppVolumeMountPath","ParameterValue":"/var/jenkins"},{"ParameterKey":"AppVolumeSize","ParameterValue":"80"},{"ParameterKey":"AppVolumeType","ParameterValue":"gp2"},{"ParameterKey":"CfnEndpointUrl","ParameterValue":"https://cloudformation.us-east-1.amazonaws.com"},{"ParameterKey":"ChainScriptUrl","ParameterValue":"https://s3.amazonaws.com/common-tools/chain-load.sh"},{"ParameterKey":"Domainname","ParameterValue":"mydev.net"},{"ParameterKey":"EpelRepo","ParameterValue":"epel"},{"ParameterKey":"GitRepoKeyUrl","ParameterValue":""},{"ParameterKey":"Hostname","ParameterValue":"jenkins-agent3"},{"ParameterKey":"InstanceRole","ParameterValue":"JenkinsTst-IamRes-1ICSNOOM0A340-InstanceProfile-1G0LQ0SKLBJE1"},{"ParameterKey":"InstanceType","ParameterValue":"t2.medium"},{"ParameterKey":"JenkinsAgentName","ParameterValue":"CommTest"},{"ParameterKey":"JenkinsMaster","ParameterValue":"jenkins.mydev.net"},{"ParameterKey":"JenkinsUserScriptUrl","ParameterValue":"https://s3.amazonaws.com/common-tools/jenkins-agent_userscript.sh"},{"ParameterKey":"KeyPairName","ParameterValue":"deploy-key-dev"},{"ParameterKey":"NoPublicIp","ParameterValue":"true"},{"ParameterKey":"NoReboot","ParameterValue":"false"},{"ParameterKey":"NoUpdates","ParameterValue":"false"},{"ParameterKey":"NpmSetupUri","ParameterValue":"https://rpm.nodesource.com/setup_8.x"},{"ParameterKey":"PipRpm","ParameterValue":"python2-pip"},{"ParameterKey":"PrivateIp","ParameterValue":""},{"ParameterKey":"ProvisionUser","ParameterValue":"provision"},{"ParameterKey":"PypiIndexUrl","ParameterValue":"https://pypi.org/simple"},{"ParameterKey":"PyStache","ParameterValue":"pystache"},{"ParameterKey":"SecurityGroupIds","ParameterValue":"sg-c59c31fd93dc6f5c7,sg-7044673ecae45bd61"},{"ParameterKey":"SubnetId","ParameterValue":"subnet-969c2629"},{"ParameterKey":"ZapAgentUrl","ParameterValue":"https://github.com/zaproxy/zaproxy/releases/download/2.6.0/ZAP_2.6.0_Linux.tar.gz"}]
~~~~

With the highly-parameterized nature of the Jenkins Agent-node templates, specifying parameter-values directly within the CLI becomes practically impossible.

The following sub-sections assume:
- The template user has previously used `aws configure` to set a default region for their commands to run within. If this is not the case, it will be necessary to insert the `--region <REGION_NAME>` option to the CLI command
- The template user is not attempting to execute the command against a non-default account-profile. If this is not the case, it will be necessary to insert the `--profile <PROFILE_NAME>` option to the CLI command

#### Locally Hosted Template

When referencing a locally-hosted file, one uses the `--template-body` parameter. Depending on the OS type being launched from specifying the path to the template may require using the `file://` URI. Similarly, if using a parameters file, one may need to use the `file://` URI. The resulting command will look like:

~~~~
aws cloudformation create-stack --stack-name make_jenkins_EC2-Agent-Linux-instance.tmplt.json \
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

`file:///home/myuser/GIT/cfn-jenkins/Templates/make_jenkins_EC2-Agent-Linux-instance.tmplt.json`

and typing:

`file://make_jenkins_EC2-Agent-Linux-instance.tmplt.json`

#### S3-Hosted Template

When referencing a S3-hosted file, one uses the `--template-url` parameter. If using a parameters file, one may need to use the `file://` URI. The resulting command will look like:

~~~~
aws cloudformation create-stack --stack-name make_jenkins_EC2-Agent-Linux-autoscale.tmplt.json \
  --template-url https://s3.<REGION>.<AMAZON_TLD>/<BUCKET_NAME>/<TEMPLATE_NAME> \
  --parameters file:///<FULLY_QUALIFIED>/<PATH>/<TO>/<PARAMETER_FILE>
~~~~

Or:

~~~~
aws cloudformation create-stack --stack-name make_jenkins_EC2-Agent-Linux-instance.tmplt.json \
  --template-url https://s3.<REGION>.<AMAZON_TLD>/<BUCKET_NAME>/<TEMPLATE_NAME> \
  --parameters file://<RELATIVE>/<PATH>/<TO>/<PARAMETER_FILE>
~~~~

### AWS Web UI

(TBD)


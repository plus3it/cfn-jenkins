pipeline {

    agent any

    options {
        buildDiscarder(
            logRotator(
                numToKeepStr: '5',
                daysToKeepStr: '30',
                artifactDaysToKeepStr: '30',
                artifactNumToKeepStr: '3'
            )
        )
        disableConcurrentBuilds()
        timeout(time: 60, unit: 'MINUTES')
    }

    environment {
          AWS_DEFAULT_REGION = "${AwsRegion}"
          AWS_CA_BUNDLE = '/etc/pki/tls/certs/ca-bundle.crt'
          REQUESTS_CA_BUNDLE = '/etc/pki/tls/certs/ca-bundle.crt'
      }

    parameters {
        string(name: 'AwsRegion', defaultValue: 'us-east-1', description: 'Amazon region to deploy resources into')
        string(name: 'AwsCred', description: 'Jenkins-stored AWS credential with which to execute cloud-layer commands')
        string(name: 'GitCred', description: 'Jenkins-stored Git credential with which to execute git commands')
        string(name: 'GitProjUrl', description: 'SSH URL from which to download the Jenkins git project')
        string(name: 'GitProjBranch', description: 'Project-branch to use from the Jenkins git project')
        string(name: 'CfnStackRoot', description: 'Unique token to prepend to all stack-element names')
        string(name: 'TemplateUrl', description: 'S3-hosted URL for the EC2 template file')
        string(name: 'AdminPubkeyURL', description: 'URL to the administrator pub keys')
        string(name: 'AgentAuthToken', description: 'Authorization token')
        string(name: 'AmiId', description: 'ID of the AMI to launch')
        string(name: 'AppVolumeDevice', defaultValue: 'false', description: 'Whether to mount an extra EBS volume. Leave as default (\"false\") to launch without an extra application volume')
        string(name: 'AppVolumeMountPath', defaultValue: '/var/jenkins', description: 'Filesystem path to mount the extra app volume. Ignored if \"AppVolumeDevice\" is blank')
        string(name: 'AppVolumeSize', defaultValue: '20', description: 'Size in GB of the EBS volume to create. Ignored if \"AppVolumeDevice\" is blank')
        string(name: 'AppVolumeType', defaultValue: 'gp2', description: 'Type of EBS volume to create. Ignored if \"AppVolumeDevice\" is blank')
        string(name: 'CfnEndpointUrl', defaultValue: 'https://cloudformation.us-east-1.amazonaws.com', description: 'URL of the Cloudformation endpoint')
        string(name: 'ChainScriptUrl', defaultValue: '', description: 'The URL of the chain load script')
        string(name: 'Domainname', description: 'The domain name')
        string(name: 'EpelRepo', defaultValue: 'epel', description: 'An alphanumeric string that represents the EPEL yum repo s label')
        string(name: 'GitRepoKeyUrl', defaultValue: '', description: '(Optional) If hosting the chain-script (or its sub-scripts) in an authenticated git-repos, provide the URL of the git repository-key')
        string(name: 'Hostname', description: 'The host name for the EC2 instance')
        string(name: 'InstanceRole', defaultValue: '', description: 'The IAM role to apply to the instance')
        string(name: 'InstanceType', defaultValue: 't2.small', description: 'Amazon EC2 instance type')
        string(name: 'JenkinsAgentName', description: 'The name to use for the agent')
        string(name: 'JenkinsMaster', description: 'The URL of the Jenkins master')
        string(name: 'JenkinsUserScriptUrl', description: 'The URL of the Jenkins user script')
        string(name: 'KeyPairName', description: 'Public/private key pairs allow you to securely connect to your instance after it launches')
        string(name: 'NoPublicIp', defaultValue: 'true', description: 'Controls whether to assign the instance a public IP. Recommended to leave at \"true\" _unless_ launching in a public subnet')
        string(name: 'NoReboot', defaultValue: 'false', description: 'Controls whether to reboot the instance as the last step of cfn-init execution')
        string(name: 'NoUpdates', defaultValue: 'false', description: 'Controls whether to run yum update during a stack update (on the initial instance launch, SystemPrep _always_ installs updates)')
        string(name: 'NpmSetupUri', defaultValue: 'https://rpm.nodesource.com/setup_8.x', description: 'URI of node.js NPM setup')
        string(name: 'PipRpm', description: 'Name of preferred pip RPM')
        string(name: 'PrivateIp', defaultValue: '', description: '(Optional) Set a static, primary private IP. Leave blank to auto-select a free IP')
        string(name: 'ProvisionUser', defaultValue: 'jenkagent', description: 'Default login user account name')
        string(name: 'PypiIndexUrl', defaultValue: 'https://pypi.org/simple', description: 'URL for pypi')
        string(name: 'PyStache', description: 'Name of preferred pystache RPM')
        string(name: 'SecurityGroupIds', description: 'The security groups to apply to the instance')
        string(name: 'SubnetId', description: 'specific subnet to deploy into')
        string(name: 'ZapAgentUrl', description: 'The URL for zaproxy')

    }

    stages {
        stage ('Prepare Agent Environment') {
            steps {
                deleteDir()
                git branch: "${GitProjBranch}",
                    credentialsId: "${GitCred}",
                    url: "${GitProjUrl}"
                withCredentials(
                    [
                        [$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: "${AwsCred}", secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'],
                        sshUserPrivateKey(credentialsId: "${GitCred}", keyFileVariable: 'SSH_KEY_FILE', passphraseVariable: 'SSH_KEY_PASS', usernameVariable: 'SSH_KEY_USER')
                    ]
                ) {
                    writeFile file: 'InfraStack.parms.json',
                        text: /
                            [
                                {
                                    "ParameterKey": "AdminPubkeyURL",
                                    "ParameterValue": "${env.AdminPubkeyURL}"
                                },
                                {
                                    "ParameterKey": "AgentAuthToken",
                                    "ParameterValue": "${env.AgentAuthToken}"
                                },
                                {
                                    "ParameterKey": "AmiId",
                                    "ParameterValue": "${env.AmiId}"
                                },
                                {
                                    "ParameterKey": "AppVolumeDevice",
                                    "ParameterValue": "${env.AppVolumeDevice}"
                                },
                                {
                                    "ParameterKey": "AppVolumeMountPath",
                                    "ParameterValue": "${env.AppVolumeMountPath}"
                                },
                                {
                                    "ParameterKey": "AppVolumeSize",
                                    "ParameterValue": "${env.AppVolumeSize}"
                                },
                                {
                                    "ParameterKey": "AppVolumeType",
                                    "ParameterValue": "${env.AppVolumeType}"
                                },
                                {
                                    "ParameterKey": "CfnEndpointUrl",
                                    "ParameterValue": "${env.CfnEndpointUrl}"
                                },
                                {
                                    "ParameterKey": "ChainScriptUrl",
                                    "ParameterValue": "__SIGNED_URL__"
                                },
                                {
                                    "ParameterKey": "Domainname",
                                    "ParameterValue": "${env.Domainname}"
                                },
                                {
                                    "ParameterKey": "EpelRepo",
                                    "ParameterValue": "${env.EpelRepo}"
                                },
                                {
                                    "ParameterKey": "GitRepoKeyUrl",
                                    "ParameterValue": "${env.GitRepoKeyUrl}"
                                },
                                {
                                    "ParameterKey": "Hostname",
                                    "ParameterValue": "${env.Hostname}"
                                },
                                {
                                    "ParameterKey": "InstanceRole",
                                    "ParameterValue": "${env.InstanceRole}"
                                },
                                {
                                    "ParameterKey": "InstanceType",
                                    "ParameterValue": "${env.InstanceType}"
                                },
                                {
                                    "ParameterKey": "JenkinsAgentName",
                                    "ParameterValue": "${env.JenkinsAgentName}"
                                },
                                {
                                    "ParameterKey": "JenkinsMaster",
                                    "ParameterValue": "${env.JenkinsMaster}"
                                },
                                {
                                    "ParameterKey": "JenkinsUserScriptUrl",
                                    "ParameterValue": "${env.JenkinsUserScriptUrl}"
                                },
                                {
                                    "ParameterKey": "KeyPairName",
                                    "ParameterValue": "${env.KeyPairName}"
                                },
                                {
                                    "ParameterKey": "NoPublicIp",
                                    "ParameterValue": "${env.NoPublicIp}"
                                },
                                {
                                    "ParameterKey": "NoReboot",
                                    "ParameterValue": "${env.NoReboot}"
                                },
                                {
                                    "ParameterKey": "NoUpdates",
                                    "ParameterValue": "${env.NoUpdates}"
                                },
                                {
                                    "ParameterKey": "NpmSetupUri",
                                    "ParameterValue": "${env.NpmSetupUri}"
                                },
                                {
                                    "ParameterKey": "PipRpm",
                                    "ParameterValue": "${env.PipRpm}"
                                },
                                {
                                    "ParameterKey": "PrivateIp",
                                    "ParameterValue": "${env.PrivateIp}"
                                },
                                {
                                    "ParameterKey": "ProvisionUser",
                                    "ParameterValue": "${env.ProvisionUser}"
                                },
                                {
                                    "ParameterKey": "PypiIndexUrl",
                                    "ParameterValue": "${env.PypiIndexUrl}"
                                },
                                {
                                    "ParameterKey": "PyStache",
                                    "ParameterValue": "${env.PyStache}"
                                },
                                {
                                    "ParameterKey": "SecurityGroupIds",
                                    "ParameterValue": "${env.SecurityGroupIds}"
                                },
                                {
                                    "ParameterKey": "SubnetId",
                                    "ParameterValue": "${env.SubnetId}"
                                },
                                {
                                    "ParameterKey": "ZapAgentUrl",
                                    "ParameterValue": "${env.ZapAgentUrl}"
                                }
                            ]
                        /
                }
            }
        }
        stage ('Prepare AWS Environment') {
            options {
                timeout(time: 1, unit: 'HOURS')
            }
            steps {
                withCredentials(
                    [
                        [$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: "${AwsCred}", secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'],
                        sshUserPrivateKey(credentialsId: "${GitCred}", keyFileVariable: 'SSH_KEY_FILE', passphraseVariable: 'SSH_KEY_PASS', usernameVariable: 'SSH_KEY_USER')
                    ]
                ) {
                    sh '''#!/bin/bash
                        echo "Attempting to delete any active ${CfnStackRoot} stacks... "
                        aws --region "${AwsRegion}" cloudformation delete-stack --stack-name "${CfnStackRoot}"

                        aws cloudformation wait stack-delete-complete --stack-name ${CfnStackRoot} --region ${AwsRegion}
                    '''
                }
            }
        }
        stage ('Launch Jenkins Master Stack') {
            options {
                timeout(time: 1, unit: 'HOURS')
            }
            steps {
                withCredentials(
                    [
                        [$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: "${AwsCred}", secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'],
                        sshUserPrivateKey(credentialsId: "${GitCred}", keyFileVariable: 'SSH_KEY_FILE', passphraseVariable: 'SSH_KEY_PASS', usernameVariable: 'SSH_KEY_USER')
                    ]
                ) {
                    sh '''#!/bin/bash -xe
                        printf 'export SIGNED_URL="%s"' "\$( aws s3 presign ${ChainScriptUrl} )" > /tmp/SIGNED_URL.txt
                        source /tmp/SIGNED_URL.txt
                        printenv | sort
                        echo "Attempting to create stack ${CfnStackRoot}..."
                        aws --region "${AwsRegion}" cloudformation create-stack --stack-name "${CfnStackRoot}" \
                          --disable-rollback --capabilities CAPABILITY_NAMED_IAM \
                          --template-url "${TemplateUrl}" \
                          --parameters file://<( sed "s#__SIGNED_URL__#${SIGNED_URL//&/\\&}#" InfraStack.parms.json )

                        aws cloudformation wait stack-create-complete --stack-name ${CfnStackRoot} --region ${AwsRegion}
                    '''
                }
            }
        }
    }
}

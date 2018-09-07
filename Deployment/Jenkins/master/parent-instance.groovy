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
        string(name: 'JenkinsListenPort', defaultValue: '443', description: 'TCP Port number on which the Jenkins ELB listens for requests')
        string(name: 'JenkinsListenerCert', defaultValue: '', description: 'Name/ID of the ACM-managed SSL Certificate securing the public listener')
        string(name: 'JenkinsServicePort', defaultValue: '8080', description: 'TCP Port number that the Jenkins host listens to')
        string(name: 'JenkinsAgentPort', defaultValue: '', description: 'TCP Port number that the Jenkins agent-hosts connect through')
        string(name: 'BackendTimeout', defaultValue: '600', description: 'How long (in seconds) back-end connection may be idle before attempting session-cleanup')
        string(name: 'JenkinsBackupBucket', description: '(Optional: will be randomly named if left un-set) Name to give to S3 Bucket used for longer-term retention of backups')
        string(name: 'ProxyPrettyName', description: 'A short human friendly label to assign to the ELB (no capital letters)')
        string(name: 'RolePrefix', description: '(Optional) Prefix to apply to IAM role')
        string(name: 'HaSubnets', description: 'Subnets to deploy service-elements to: select as many private-subnets as are available in VPC - selecting one from each Availability Zone')
        string(name: 'TargetVPC', description: 'ID of the VPC to deploy cluster nodes into')
        string(name: 'AmiId', description: 'ID of the AMI to launch')
        string(name: 'AppVolumeDevice', defaultValue: '', description: 'Whether to mount an extra EBS volume. Leave as default (\"false\") to launch without an extra application volume')
        string(name: 'AppVolumeMountPath', defaultValue: '/var/lib/jenkins', description: 'Filesystem path to mount the extra app volume. Ignored if \"AppVolumeDevice\" is blank')
        string(name: 'AppVolumeSize', defaultValue: '1', description: 'Size in GB of the EBS volume to create. Ignored if \"AppVolumeDevice\" is blank')
        string(name: 'AppVolumeType', defaultValue: 'gp2', description: 'Type of EBS volume to create. Ignored if \"AppVolumeDevice\" is blank')
        string(name: 'DnsSuffix', description: 'Suffix for Jenkins hostname and DNS record')
        string(name: 'EpelRepo', defaultValue: 'epel', description: 'An alphanumeric string that represents the EPEL yum repo s label')
        string(name: 'InstanceType', defaultValue: 't2.micro', description: 'Amazon EC2 instance type')
        string(name: 'JenkinsAppinstallScriptUrl', description: 'URL of Jenkins application-installer script')
        string(name: 'JenkinsOsPrepScriptUrl', description: 'URL for OS Prep script')
        string(name: 'JenkinsPassesSsh', defaultValue: 'false', description: 'Whether to allow SSH passthrough to Jenkins master')
        string(name: 'JenkinsRepoKeyURL', defaultValue: 'https://pkg.jenkins.io/redhat-stable/jenkins.io.key', description: 'URL to the Jenkins yum-repository GPG key')
        string(name: 'JenkinsRepoURL', defaultValue: 'http://pkg.jenkins.io/redhat-stable', description: 'URL to the Jenkins yum-repository')
        string(name: 'JenkinsRpmName', description: 'Name of Jenkins RPM to install. Include release version if other-than-latest is desired. Example values would be: jenkins, jenkins-2.*, jenkins-X.Y.Z, etc')
        string(name: 'KeyPairName', description: 'Public/private key pairs allow you to securely connect to your instance after it launches')
        string(name: 'NoPublicIp', defaultValue: 'true', description: 'Controls whether to assign the instance a public IP. Recommended to leave at \"true\" _unless_ launching in a public subnet')
        string(name: 'NoReboot', defaultValue: 'false', description: 'Controls whether to reboot the instance as the last step of cfn-init execution')
        string(name: 'NoUpdates', defaultValue: 'false', description: 'Controls whether to run yum update during a stack update (on the initial instance launch, SystemPrep _always_ installs updates)')
        string(name: 'PipIndexFips', defaultValue: 'https://pypi.org/simple/', description: 'URL of pip index  that is compatible with FIPS 140-2 requirements')
        string(name: 'PipRpm', defaultValue: 'python2-pip', description: 'Name of preferred pip RPM')
        string(name: 'ProvisionUser', defaultValue: 'autojenk', description: 'Default login user account name')
        string(name: 'PyStache', defaultValue: 'pystache', description: 'Name of preferred pystache RPM')
        string(name: 'ServerHostname', defaultValue: 'jenkins-master', description: 'Suffix for Jenkins hostname and DNS record')
        string(name: 'ServiceTld', defaultValue: 'amazonaws.com', description: 'TLD of the IAMable service-name')
        string(name: 'AdminPubkeyURL', description: 'URL to the administrator pub keys')
        string(name: 'BackupFolder', description: 'Folder in S3 Bucket to host backups of Jenkins config-data')
        string(name: 'BucketTemplate', description: 'link to bucket template')
        string(name: 'Ec2Template', description: 'Link to EC2 template')
        string(name: 'ElbTemplate', description: 'Link to ELB template')
        string(name: 'IamRoleTemplate', description: 'Link to IAM template')
        string(name: 'SecurityGroupTemplate', description: 'Link to SG template')
        string(name: 'BackupCronURL', description: 'Link to crontab file for backups')
        string(name: 'SubnetId', description: 'specific subnet to deploy into')
    }

    stages {
        stage ('Prepare Agent Environment') {
            steps {
                deleteDir()
                git branch: "${GitProjBranch}",
                    credentialsId: "${GitCred}",
                    url: "${GitProjUrl}"
                writeFile file: 'parent.instance.parms.json',
                    text: /
                    [
                      {
                        "ParameterKey": "JenkinsListenPort",
                        "ParameterValue": "${env.JenkinsListenPort}"
                      },
                      {
                        "ParameterKey": "JenkinsListenerCert",
                        "ParameterValue": "${env.JenkinsListenerCert}"
                      },
                      {
                        "ParameterKey": "JenkinsServicePort",
                        "ParameterValue": "${env.JenkinsServicePort}"
                      },
                      {
                        "ParameterKey": "BackendTimeout",
                        "ParameterValue": "${env.BackendTimeout}"
                      },
                      {
                        "ParameterKey": "JenkinsBackupBucket",
                        "ParameterValue": "${env.JenkinsBackupBucket}"
                      },
                      {
                        "ParameterKey": "ProxyPrettyName",
                        "ParameterValue": "${env.ProxyPrettyName}"
                      },
                      {
                        "ParameterKey": "RolePrefix",
                        "ParameterValue": "${env.RolePrefix}"
                      },
                      {
                        "ParameterKey": "HaSubnets",
                        "ParameterValue": "${env.HaSubnets}"
                      },
                      {
                        "ParameterKey": "JenkinsAgentPort",
                        "ParameterValue": "${env.JenkinsAgentPort}"
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
                        "ParameterKey": "DnsSuffix",
                        "ParameterValue": "${env.DnsSuffix}"
                      },
                      {
                        "ParameterKey": "EpelRepo",
                        "ParameterValue": "${env.EpelRepo}"
                      },
                      {
                        "ParameterKey": "InstanceType",
                        "ParameterValue": "${env.InstanceType}"
                      },
                      {
                        "ParameterKey": "JenkinsAppinstallScriptUrl",
                        "ParameterValue": "${env.JenkinsAppinstallScriptUrl}"
                      },
                      {
                        "ParameterKey": "JenkinsOsPrepScriptUrl",
                        "ParameterValue": "${env.JenkinsOsPrepScriptUrl}"
                      },
                      {
                        "ParameterKey": "JenkinsPassesSsh",
                        "ParameterValue": "${env.JenkinsPassesSsh}"
                      },
                      {
                        "ParameterKey": "JenkinsRepoKeyURL",
                        "ParameterValue": "${env.JenkinsRepoKeyURL}"
                      },
                      {
                        "ParameterKey": "JenkinsRpmName",
                        "ParameterValue": "${env.JenkinsRpmName}"
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
                        "ParameterKey": "PipIndexFips",
                        "ParameterValue": "${env.PipIndexFips}"
                      },
                      {
                        "ParameterKey": "PipRpm",
                        "ParameterValue": "${env.PipRpm}"
                      },
                      {
                        "ParameterKey": "ProvisionUser",
                        "ParameterValue": "${env.ProvisionUser}"
                      },
                      {
                        "ParameterKey": "PyStache",
                        "ParameterValue": "${env.PyStache}"
                      },
                      {
                        "ParameterKey": "ServerHostname",
                        "ParameterValue": "${env.ServerHostname}"
                      },
                      {
                        "ParameterKey": "ServiceTld",
                        "ParameterValue": "${env.ServiceTld}"
                      },
                      {
                        "ParameterKey": "AdminPubkeyURL",
                        "ParameterValue": "${env.AdminPubkeyURL}"
                      },
                      {
                        "ParameterKey": "BackupFolder",
                        "ParameterValue": "${env.BackupFolder}"
                      },
                      {
                        "ParameterKey": "BucketTemplate",
                        "ParameterValue": "${env.BucketTemplate}"
                      },
                      {
                        "ParameterKey": "Ec2Template",
                        "ParameterValue": "${env.Ec2Template}"
                      },
                      {
                        "ParameterKey": "ElbTemplate",
                        "ParameterValue": "${env.ElbTemplate}"
                      },
                      {
                        "ParameterKey": "IamRoleTemplate",
                        "ParameterValue": "${env.IamRoleTemplate}"
                      },
                      {
                        "ParameterKey": "SecurityGroupTemplate",
                        "ParameterValue": "${env.SecurityGroupTemplate}"
                      },
                      {
                        "ParameterKey": "TargetVPC",
                        "ParameterValue": "${env.TargetVPC}"
                      },
                      {
                      "ParameterKey": "BackupCronURL",
                      "ParameterValue": "${env.BackupCronURL}"
                      },
                      {
                      "ParameterKey": "SubnetId",
                      "ParameterValue": "${env.SubnetId}"
                      }
                    ]
                   /
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
                        echo "Attempting to delete any active ${CfnStackRoot}-ParInst-${BUILD_NUMBER} stacks... "
                        aws --region "${AwsRegion}" cloudformation delete-stack --stack-name "${CfnStackRoot}-ParInst-${BUILD_NUMBER}"

                        aws cloudformation wait stack-delete-complete --stack-name ${CfnStackRoot}-ParInst-${BUILD_NUMBER} --region ${AwsRegion}
                    '''
                }
            }
        }
        stage ('Launch Jenkins Master Parent Instance Stack') {
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
                        echo "Attempting to create stack ${CfnStackRoot}-ParInst-${BUILD_NUMBER}..."
                        aws --region "${AwsRegion}" cloudformation create-stack --stack-name "${CfnStackRoot}-ParInst-${BUILD_NUMBER}" \
                          --disable-rollback --capabilities CAPABILITY_NAMED_IAM \
                          --template-url "${TemplateUrl}" \
                          --parameters file://parent.instance.parms.json

                        aws cloudformation wait stack-create-complete --stack-name ${CfnStackRoot}-ParInst-${BUILD_NUMBER} --region ${AwsRegion}
                    '''
                }
            }
        }
    }
}

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
    string(name: 'ElbName', description: 'ARN of the ELB to attach the EC2 instance')
    string(name: 'AdminPubkeyURL', description: 'URL the file containing the admin users SSH public keys')
    string(name: 'AmiDistro', defaultValue: 'CentOS', description: 'Linux distro of the AMI')
    string(name: 'AmiId', description: 'ID of the AMI to launch')
    string(name: 'AppVolumeDevice', defaultValue: '', description: 'Device to mount an extra EBS volume. Leave blank to launch without an extra application volume')
    string(name: 'AppVolumeMountPath', defaultValue: '/var/lib/jenkins', description: 'Filesystem path to mount the extra app volume. Ignored if \"AppVolumeDevice\" is blank')
    string(name: 'AppVolumeSize', defaultValue: '20', description: 'Size in GB of the EBS volume to create. Ignored if \"AppVolumeDevice\" is blank')
    string(name: 'AppVolumeType', defaultValue: 'gp2', description: 'Type of EBS volume to create. Ignored if \"AppVolumeDevice\" is blank')
    string(name: 'BackupBucket', description: 'S3 Bucket to host backups of Jenkins config-data')
    string(name: 'BackupCronURL', description: 'URL to the Jenkins backup cron-file')
    string(name: 'BackupFolder', description: 'Folder in S3 Bucket to host backups of Jenkins config-data')
    string(name: 'CfnEndpointUrl', defaultValue: '', description: 'URL to the CloudFormation Endpoint. e.g. https://cloudformation.us-east-1.amazonaws.com')
    string(name: 'DnsSuffix', description: 'Suffix for Jenkins hostname and DNS record')
    string(name: 'EpelRepo', defaultValue: 'epel', description: 'Name of networks EPEL repo')
    string(name: 'InstanceRole', defaultValue: '', description: 'IAM instance role to apply to the instance')
    string(name: 'InstanceType', defaultValue: 't2.micro', description: 'Amazon EC2 instance type')
    string(name: 'JenkinsAppinstallScriptUrl', description: 'URL of Jenkins application-installer script')
    string(name: 'JenkinsOsPrepScriptUrl', description: 'URL of OS-preparation script')
    string(name: 'JenkinsRepoKeyURL', defaultValue: 'https://pkg.jenkins.io/redhat-stable/jenkins.io.key', description: 'URL to the Jenkins yum-repository GPG key')
    string(name: 'JenkinsRepoURL', defaultValue: 'http://pkg.jenkins.io/redhat-stable', description: 'URL to the Jenkins yum-repository')
    string(name: 'JenkinsRpmName', description: 'Name of Jenkins RPM to install. Include release version if other-than-latest is desired. Example values would be: jenkins, jenkins-2.*, jenkins-X.Y.Z, etc')
    string(name: 'KeyPairName', description: 'Public/private key pairs allow you to securely connect to your instance after it launches')
    string(name: 'NoPublicIp', defaultValue: 'true', description: 'Controls whether to assign the instance a public IP. Recommended to leave at \"true\" _unless_ launching in a public subnet')
    string(name: 'NoReboot', defaultValue: 'false', description: 'Controls whether to reboot the instance as the last step of cfn-init execution')
    string(name: 'NoUpdates', defaultValue: 'false', description: 'Controls whether to run yum update during a stack update (on the initial instance launch, SystemPrep _always_ installs updates)')
    string(name: 'PipIndexFips', defaultValue: 'https://pypi.org/simple/', description: 'URL of pip index that is compatible with FIPS 140-2 requirements')
    string(name: 'PipRpm', defaultValue: 'python2-pip', description: 'Name of preferred pip RPM')
    string(name: 'ProvisionUser', defaultValue: 'autojenk', description: 'Default login user account name')
    string(name: 'PyStache', defaultValue: 'pystache', description: 'Name of preferred pystache RPM')
    string(name: 'SecurityGroupIds', description: 'List of security groups to apply to the instance')
    string(name: 'ServerHostname', defaultValue: 'jenkins-master', description: 'Suffix for Jenkins hostname and DNS record')
    string(name: 'SubnetIds', description: 'Subnet to associate to the Instance')
    string(name: 'WatchmakerConfig', defaultValue: '', description: 'Optional URL to a Watchmaker config file')
    string(name: 'WatchmakerEnvironment', defaultValue: '', description: 'Environment in which the instance is being deployed')

}

stages {
    stage ('Prepare Instance Environment') {
        steps {
            deleteDir()
            git branch: "${GitProjBranch}",
                credentialsId: "${GitCred}",
                url: "${GitProjUrl}"
            writeFile file: 'master.ec2.instance.parms.json',
                text: /
                [
                {
                  "ParameterKey": "AdminPubkeyURL",
                  "ParameterValue": "${env.AdminPubkeyURL}"
                },
                {
                  "ParameterKey": "AmiDistro",
                  "ParameterValue": "${env.AmiDistro}"
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
                  "ParameterKey": "BackupBucket",
                  "ParameterValue": "${env.BackupBucket}"
                },
                {
                  "ParameterKey": "BackupCronURL",
                  "ParameterValue": "${env.BackupCronURL}"
                },
                {
                  "ParameterKey": "BackupFolder",
                  "ParameterValue": "${env.BackupFolder}"
                },
                {
                  "ParameterKey": "CfnEndpointUrl",
                  "ParameterValue": "${env.CfnEndpointUrl}"
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
                  "ParameterKey": "InstanceRole",
                  "ParameterValue": "${env.InstanceRole}"
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
                  "ParameterKey": "JenkinsRepoKeyURL",
                  "ParameterValue": "${env.JenkinsRepoKeyURL}"
                },
                {
                  "ParameterKey": "JenkinsRepoURL",
                  "ParameterValue": "${env.JenkinsRepoURL}"
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
                  "ParameterKey": "SecurityGroupIds",
                  "ParameterValue": "${env.SecurityGroupIds}"
                },
                {
                  "ParameterKey": "ServerHostname",
                  "ParameterValue": "${env.ServerHostname}"
                },
                {
                  "ParameterKey": "SubnetIds",
                  "ParameterValue": "${env.SubnetIds}"
                },
                {
                  "ParameterKey": "WatchmakerConfig",
                  "ParameterValue": "${env.WatchmakerConfig}"
                },
                {
                  "ParameterKey": "WatchmakerEnvironment",
                  "ParameterValue": "${env.WatchmakerEnvironment}"
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
                    echo "Attempting to delete any active ${CfnStackRoot}-Ec2Inst-${BUILD_NUMBER} stacks... "
                    aws --region "${AwsRegion}" cloudformation delete-stack --stack-name "${CfnStackRoot}-Ec2Inst-${BUILD_NUMBER}"

                    aws cloudformation wait stack-delete-complete --stack-name ${CfnStackRoot}-Ec2Inst-${BUILD_NUMBER} --region ${AwsRegion}
                '''
            }
        }
    }
    stage ('Launch Jenkins Master EC2 Instance Stack') {
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
                    echo "Attempting to create stack ${CfnStackRoot}-Ec2Inst-${BUILD_NUMBER}..."
                    aws --region "${AwsRegion}" cloudformation create-stack --stack-name "${CfnStackRoot}-Ec2Inst-${BUILD_NUMBER}" \
                      --disable-rollback --capabilities CAPABILITY_NAMED_IAM \
                      --template-url "${TemplateUrl}" \
                      --parameters file://master.ec2.instance.parms.json

                    aws cloudformation wait stack-create-complete --stack-name ${CfnStackRoot}-Ec2Inst-${BUILD_NUMBER} --region ${AwsRegion}
                '''
            }
        }
    }
    stage ('Bind EC2 Instance to ELB') {
        options {
            timeout(time: 1, unit: 'HOURS')
        }
        steps {
            withCredentials(
                [
                    [$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: "${AwsCred}", secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']
                ]
            ) {
                sh '''#!/bin/bash
                    echo "retrieving instance ID for ${CfnStackRoot}-Ec2Inst-${BUILD_NUMBER}..."

                    InstanceId=$(aws ec2 describe-instances --filters "Name=tag:Name, Values=${CfnStackRoot}-Ec2Inst-${BUILD_NUMBER}" --query "Reservations[].Instances[].InstanceId[]" --output text)

                    echo "Attaching ${InstanceId} to ${ElbName}..."

                    aws elb register-instances-with-load-balancer --load-balancer-name ${ElbName} --instances ${InstanceId}
                '''
            }
        }
    }
}
}

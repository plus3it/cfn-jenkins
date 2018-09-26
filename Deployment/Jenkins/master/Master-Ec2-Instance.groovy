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
        string(name: 'AdminPubkeyURL', description: '(Optional) URL of file containing admin group's SSH public-keys')
        string(name: 'AmiId', description: 'ID of the AMI to launch')
        string(name: 'AppVolumeDevice', defaultValue: 'false', description: 'Decision on whether to mount an extra EBS volume. Leave as default (\'false\') to launch without an extra application volume')
        string(name: 'AppVolumeMountPath', defaultValue: '/opt/data', description: 'Filesystem path to mount the extra app volume. Ignored if \'AppVolumeDevice\' is false')
        string(name: 'AppVolumeSize', defaultValue: '1', description: 'Size in GB of the EBS volume to create. Ignored if \'AppVolumeDevice\' is false')
        string(name: 'AppVolumeType', defaultValue: 'gp2', description: 'Type of EBS volume to create. Ignored if \'AppVolumeDevice\' is false')
        string(name: 'BackupBucket', description: 'S3 Bucket to host backups of Jenkins config-data')
        string(name: 'BackupFolder', description: 'Folder in S3 Bucket to host backups of Jenkins config-data')
        string(name: 'CfnBootstrapUtilsUrl', defaultValue: 'https://s3.amazonaws.com/cloudformation-examples/aws-cfn-bootstrap-latest.tar.gz', description: 'URL to aws-cfn-bootstrap-latest.tar.gz')
        string(name: 'CfnEndpointUrl', defaultValue: 'https://cloudformation.us-east-1.amazonaws.com', description: '(Optional) URL to the CloudFormation Endpoint. e.g. https://cloudformation.us-east-1.amazonaws.com')
        string(name: 'CfnGetPipUrl', defaultValue: 'https://bootstrap.pypa.io/2.6/get-pip.py', description: 'URL to get-pip.py')
        string(name: 'CloudWatchAgentUrl', defaultValue: '', description: '(Optional) S3 URL to CloudWatch Agent installer. Example: s3://amazoncloudwatch-agent/linux/amd64/latest/AmazonCloudWatchAgent.zip')
        string(name: 'EpelRepo', defaultValue: 'epel', description: 'Name of network's EPEL repo.')
        string(name: 'InstanceRoleName', defaultValue: '', description: '(Optional) IAM instance role to apply to the instance')
        string(name: 'InstanceRoleProfile', defaultValue: '', description: '(Optional) IAM instance-role profile to apply to the instance(s)')
        string(name: 'InstanceType', defaultValue: 't2.xlarge', description: 'Amazon EC2 instance type')
        string(name: 'JenkinsAppinstallScriptUrl', description: 'URL of Jenkins application-installer script.')
        string(name: 'JenkinsOsPrepScriptUrl', description: 'URL of OS-preparation script.')
        string(name: 'JenkinsRepoKeyURL', defaultValue: 'https://pkg.jenkins.io/redhat-stable/jenkins.io.key', description: 'URL to the Jenkins yum-repository GPG key')
        string(name: 'JenkinsRepoURL', defaultValue: 'http://pkg.jenkins.io/redhat-stable', description: 'URL to the Jenkins yum-repository')
        string(name: 'JenkinsRpmName', description: 'Name of Jenkins RPM to install. Include release version if 'other-than-latest' is desired. Example values would be: jenkins, jenkins-2.*, jenkins-X.Y.Z, etc.')
        string(name: 'KeyPairName', description: 'Public/private key pair allowing an operator to securely connect to instance immediately after instance's SSHD comes online')
        string(name: 'NoPublicIp', defaultValue: 'true', description: 'Controls whether to assign the instance a public IP. Recommended to leave at \'true\' _unless_ launching in a public subnet')
        string(name: 'NoReboot', defaultValue: 'false', description: 'Controls whether to reboot the instance as the last step of cfn-init execution')
        string(name: 'NoUpdates', defaultValue: 'false', description: 'Controls whether to run yum update during a stack update (on the initial instance launch, Watchmaker _always_ installs updates)')
        string(name: 'PrivateIp', defaultValue: '', description: '(Optional) Set a static, primary private IP. Leave blank to auto-select a free IP')
        string(name: 'ProvisionUser', defaultValue: 'ec2-user', description: 'Name for remote-administration account')
        string(name: 'PypiIndexUrl', defaultValue: 'https://pypi.org/simple', description: 'URL to the PyPi Index')
        string(name: 'RootVolumeSize', defaultValue: '20', description: 'Size in GB of the EBS volume to create. If smaller than AMI defaul, create operation will fail; If larger, root device-volume's partition size will be increased')
        string(name: 'SecurityGroupIds', description: 'List of security groups to apply to the instance')
        string(name: 'SubnetId', description: 'ID of the subnet to assign to the instance')
        string(name: 'ToggleCfnInitUpdate', defaultValue: 'A', description: 'A/B toggle that forces a change to instance metadata, triggering the cfn-init update sequence')
        string(name: 'WatchmakerAdminGroups', defaultValue: '', description: '(Optional) Colon-separated list of domain groups that should have admin permissions on the EC2 instance')
        string(name: 'WatchmakerAdminUsers', defaultValue: '', description: '(Optional) Colon-separated list of domain users that should have admin permissions on the EC2 instance')
        string(name: 'WatchmakerComputerName', defaultValue: '', description: '(Optional) Sets the hostname/computername within the OS')
        string(name: 'WatchmakerConfig', defaultValue: '', description: '(Optional) Path to a Watchmaker config file.  The config file path can be a remote source (i.e. http[s]://, s3://) or local directory (i.e. file://)')
        string(name: 'WatchmakerEnvironment', defaultValue: '', description: 'Environment in which the instance is being deployed')
        string(name: 'WatchmakerOuPath', defaultValue: '', description: '(Optional) DN of the OU to place the instance when joining a domain. If blank and \'WatchmakerEnvironment\' enforces a domain join, the instance will be placed in a default container. Leave blank if not joining a domain, or if \'WatchmakerEnvironment\' is \'false\'')
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
                                "ParameterKey": "BackupFolder",
                                "ParameterValue": "${env.BackupFolder}"
                            },
                            {
                                "ParameterKey": "CfnBootstrapUtilsUrl",
                                "ParameterValue": "${env.CfnBootstrapUtilsUrl}"
                            },
                            {
                                "ParameterKey": "CfnEndpointUrl",
                                "ParameterValue": "${env.CfnEndpointUrl}"
                            },
                            {
                                "ParameterKey": "CfnGetPipUrl",
                                "ParameterValue": "${env.CfnGetPipUrl}"
                            },
                            {
                                "ParameterKey": "CloudWatchAgentUrl",
                                "ParameterValue": "${env.CloudWatchAgentUrl}"
                            },
                            {
                                "ParameterKey": "EpelRepo",
                                "ParameterValue": "${env.EpelRepo}"
                            },
                            {
                                "ParameterKey": "InstanceRoleName",
                                "ParameterValue": "${env.InstanceRoleName}"
                            },
                            {
                                "ParameterKey": "InstanceRoleProfile",
                                "ParameterValue": "${env.InstanceRoleProfile}"
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
                                "ParameterKey": "RootVolumeSize",
                                "ParameterValue": "${env.RootVolumeSize}"
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
                                "ParameterKey": "ToggleCfnInitUpdate",
                                "ParameterValue": "${env.ToggleCfnInitUpdate}"
                            },
                            {
                                "ParameterKey": "WatchmakerAdminGroups",
                                "ParameterValue": "${env.WatchmakerAdminGroups}"
                            },
                            {
                                "ParameterKey": "WatchmakerAdminUsers",
                                "ParameterValue": "${env.WatchmakerAdminUsers}"
                            },
                            {
                                "ParameterKey": "WatchmakerComputerName",
                                "ParameterValue": "${env.WatchmakerComputerName}"
                            },
                            {
                                "ParameterKey": "WatchmakerConfig",
                                "ParameterValue": "${env.WatchmakerConfig}"
                            },
                            {
                                "ParameterKey": "WatchmakerEnvironment",
                                "ParameterValue": "${env.WatchmakerEnvironment}"
                            },
                            {
                                "ParameterKey": "WatchmakerOuPath",
                                "ParameterValue": "${env.WatchmakerOuPath}"
                            }
                        ]
                    /
                }
            }
        stage ('Prepare AWS Environment') {
            steps {
                withCredentials(
                    [
                        [$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: "${AwsCred}", secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'],
                        sshUserPrivateKey(credentialsId: "${GitCred}", keyFileVariable: 'SSH_KEY_FILE', passphraseVariable: 'SSH_KEY_PASS', usernameVariable: 'SSH_KEY_USER')
                    ]
                ) {
                    sh '''#!/bin/bash
                        echo "Attempting to delete any active ${CfnStackRoot}-Ec2Inst-${BUILD_NUMBER} stacks..."
                        aws --region "${AwsRegion}" cloudformation delete-stack --stack-name "${CfnStackRoot}-Ec2Inst-${BUILD_NUMBER}"

                        aws cloudformation wait stack-delete-complete --stack-name ${CfnStackRoot}-Ec2Inst-${BUILD_NUMBER} --region ${AwsRegion}
                    '''
                }
            }
        }
        stage ('Launch Jenkins Master EC2 Instance Stack') {
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

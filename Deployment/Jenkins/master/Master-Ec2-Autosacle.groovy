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
        string(name: 'AdminPubkeyURL', description: '(Optional) URL of file containing admin groups SSH public-keys')
        string(name: 'AmiId', description: 'ID of the AMI to launch')
        string(name: 'AppVolumeDevice', defaultValue: 'false', description: 'Decision whether to mount an extra EBS volume. Leave as default (\'false\') to launch without an extra application volume')
        string(name: 'AppVolumeMountPath', defaultValue: '/var/lib/jenkins', description: 'Filesystem path to mount the extra app volume. Ignored if \'AppVolumeDevice\' is blank')
        string(name: 'AppVolumeSize', defaultValue: '20', description: 'Size in GB of the EBS volume to create. Ignored if \'AppVolumeDevice\' is blank')
        string(name: 'AppVolumeType', defaultValue: 'gp2', description: 'Type of EBS volume to create. Ignored if \'AppVolumeDevice\' is blank')
        string(name: 'BackupBucket', description: 'S3 Bucket to host backups of Jenkins config-data')
        string(name: 'BackupFolder', description: 'Folder in S3 Bucket to host backups of Jenkins config-data')
        string(name: 'CfnBootstrapUtilsUrl', defaultValue: 'https://s3.amazonaws.com/cloudformation-examples/aws-cfn-bootstrap-latest.tar.gz', description: 'URL to aws-cfn-bootstrap-latest.tar.gz')
        string(name: 'CfnEndpointUrl', defaultValue: 'https://cloudformation.us-east-1.amazonaws.com', description: '(Optional) URL to the CloudFormation Endpoint. e.g. https://cloudformation.us-east-1.amazonaws.com')
        string(name: 'CfnGetPipUrl', defaultValue: 'https://bootstrap.pypa.io/2.6/get-pip.py', description: 'URL to get-pip.py')
        string(name: 'CloudWatchAgentUrl', defaultValue: '', description: '(Optional) S3 URL to CloudWatch Agent installer. Example: s3://amazoncloudwatch-agent/linux/amd64/latest/AmazonCloudWatchAgent.zip')
        string(name: 'DesiredCapacity', defaultValue: '1', description: 'Desired number of instances in the Autoscaling Group')
        string(name: 'EpelRepo', defaultValue: 'epel', description: 'Name of available EPEL repo.')
        string(name: 'InstanceRoleName', defaultValue: '', description: '(Optional) IAM instance role to apply to the instance')
        string(name: 'InstanceRoleProfile', defaultValue: '', description: '(Optional) IAM instance-role profile to apply to the instance(s)')
        string(name: 'InstanceType', defaultValue: 't2.xlarge', description: 'Amazon EC2 instance type')
        string(name: 'JenkinsAppinstallScriptUrl', description: 'URL of Jenkins application-installer script (Must be anonymously fetchable or embed authenticated-fetch information).')
        string(name: 'JenkinsOsPrepScriptUrl', description: 'URL of OS-preparation script (Must be anonymously fetchable or embed authenticated-fetch information).')
        string(name: 'JenkinsRepoKeyURL', defaultValue: 'https://pkg.jenkins.io/redhat-stable/jenkins.io.key', description: 'URL to the Jenkins yum-repository GPG key')
        string(name: 'JenkinsRepoURL', defaultValue: 'http://pkg.jenkins.io/redhat-stable', description: 'URL to the Jenkins yum-repository')
        string(name: 'JenkinsRpmName', description: 'Name of Jenkins RPM to install. Include release version if "other-than-latest" is desired. Example values would be: jenkins, jenkins-2.*, jenkins-X.Y.Z, etc.')
        string(name: 'KeyPairName', description: 'Public/private key pairs allow you to securely connect to your instance after it launches')
        string(name: 'LoadBalancerNames', defaultValue: '', description: 'Comma-separated string of Classic ELB Names to associate with the Autoscaling Group; conflicts with TargetGroupArns')
        string(name: 'MaxCapacity', defaultValue: '2', description: 'Maximum number of instances in the Autoscaling Group')
        string(name: 'MinCapacity', defaultValue: '1', description: 'Minimum number of instances in the Autoscaling Group')
        string(name: 'NoPublicIp', defaultValue: 'true', description: 'Controls whether to assign the instance a public IP. Recommended to leave at \'true\' _unless_ launching in a public subnet')
        string(name: 'NoReboot', defaultValue: 'false', description: 'Controls whether to reboot the instance as the last step of cfn-init execution')
        string(name: 'NoUpdates', defaultValue: 'false', description: 'Controls whether to run yum update during a stack update (on the initial instance launch, Watchmaker _always_ installs updates)')
        string(name: 'ProvisionUser', defaultValue: 'ec2-user', description: 'Name for remote-administration account')
        string(name: 'PypiIndexUrl', defaultValue: 'https://pypi.org/simple', description: 'URL to the PyPi Index')
        string(name: 'RootVolumeSize', defaultValue: '20', description: 'Size in GB of the EBS volume to create. If smaller than AMI defaul, create operation will fail; If larger, root device-volume partition size will be increased')
        string(name: 'ScaleDownSchedule', defaultValue: '', description: '(Optional) Scheduled Action in cron-format (UTC) to scale down to MinCapacity; ignored if empty or ScaleUpSchedule is unset (E.g. \'0 0 * * *\')')
        string(name: 'ScaleUpSchedule', defaultValue: '', description: '(Optional) Scheduled Action in cron-format (UTC) to scale up to MaxCapacity; ignored if empty or ScaleDownSchedule is unset (E.g. \'0 10 * * Mon-Fri\')')
        string(name: 'SecurityGroupIds', description: 'List of security groups to apply to the instance(s)')
        string(name: 'SubnetIds', description: 'List of subnets to associate to the Autoscaling Group')
        string(name: 'TargetGroupArns', defaultValue: '', description: 'Comma-separated string of Target Group ARNs to associate with the Autoscaling Group; conflicts with LoadBalancerNames')
        string(name: 'ToggleCfnInitUpdate', defaultValue: 'A', description: 'A/B toggle that forces a change to instance metadata, triggering the cfn-init update sequence')
        string(name: 'ToggleNewInstances', defaultValue: 'A', description: 'A/B toggle that forces a change to instance userdata, triggering new instances via the Autoscale update policy')
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
                writeFile file: 'Jenkins-Ec2-Master-ASG.parms.json',
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
                                "ParameterKey": "DesiredCapacity",
                                "ParameterValue": "${env.DesiredCapacity}"
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
                                "ParameterKey": "LoadBalancerNames",
                                "ParameterValue": "${env.LoadBalancerNames}"
                            },
                            {
                                "ParameterKey": "MaxCapacity",
                                "ParameterValue": "${env.MaxCapacity}"
                            },
                            {
                                "ParameterKey": "MinCapacity",
                                "ParameterValue": "${env.MinCapacity}"
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
                                "ParameterKey": "ScaleDownSchedule",
                                "ParameterValue": "${env.ScaleDownSchedule}"
                            },
                            {
                                "ParameterKey": "ScaleUpSchedule",
                                "ParameterValue": "${env.ScaleUpSchedule}"
                            },
                            {
                                "ParameterKey": "SecurityGroupIds",
                                "ParameterValue": "${env.SecurityGroupIds}"
                            },
                            {
                                "ParameterKey": "SubnetIds",
                                "ParameterValue": "${env.SubnetIds}"
                            },
                            {
                                "ParameterKey": "TargetGroupArns",
                                "ParameterValue": "${env.TargetGroupArns}"
                            },
                            {
                                "ParameterKey": "ToggleCfnInitUpdate",
                                "ParameterValue": "${env.ToggleCfnInitUpdate}"
                            },
                            {
                                "ParameterKey": "ToggleNewInstances",
                                "ParameterValue": "${env.ToggleNewInstances}"
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
                        [$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: "${AwsCred}", secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']
                    ]
                ) {
                    sh '''#!/bin/bash
                        echo "Attempting to delete any active ${CfnStackRoot}-AsgRes stacks..."
                        aws --region "${AwsRegion}" cloudformation delete-stack --stack-name "${CfnStackRoot}-AsgRes"

                        aws cloudformation wait stack-delete-complete --stack-name ${CfnStackRoot}-AsgRes --region ${AwsRegion}
                    '''
                }
            }
        }
        stage ('Launch Jenkins Master EC2 Instance Stack') {
            steps {
                withCredentials(
                    [
                        [$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: "${AwsCred}", secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']
                    ]
                ) {
                    sh '''#!/bin/bash
                        echo "Attempting to create stack ${CfnStackRoot}-AsgRes..."
                        aws --region "${AwsRegion}" cloudformation create-stack --stack-name "${CfnStackRoot}-AsgRes" \
                          --disable-rollback --capabilities CAPABILITY_NAMED_IAM \
                          --template-url "${TemplateUrl}" \
                          --parameters file://Jenkins-Ec2-Master-ASG.parms.json
                        sleep 15

                        # Pause if create is slow
                        while [[ $(
                                    aws cloudformation describe-stacks \
                                      --stack-name ${CfnStackRoot}-AsgRes \
                                      --query 'Stacks[].{Status:StackStatus}' \
                                      --out text 2> /dev/null | \
                                    grep -q CREATE_IN_PROGRESS
                                   )$? -eq 0 ]]
                        do
                           echo "Waiting for stack ${CfnStackRoot}-AsgRes to finish create process..."
                           sleep 30
                        done

                        if [[ $(
                                aws cloudformation describe-stacks \
                                  --stack-name ${CfnStackRoot}-AsgRes \
                                  --query 'Stacks[].{Status:StackStatus}' \
                                  --out text 2> /dev/null | \
                                grep -q CREATE_COMPLETE
                               )$? -eq 0 ]]
                        then
                           echo "Stack-creation successful"
                        else
                           echo "Stack-creation ended with non-successful state"
                           exit 1
                        fi
                    '''
                }
            }
        }
    }
}

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
        string(name: 'AdminPubkeyURL', description: 'URL the file containing the admin users' SSH public keys.')
        string(name: 'AmiId', description: 'ID of the AMI to launch')
        string(name: 'AppVolumeDevice', defaultValue: 'false', description: 'Decision on whether to mount an extra EBS volume. Leave as default (\'false\') to launch without an extra application volume')
        string(name: 'AppVolumeMountPath', defaultValue: '/var/lib/jenkins', description: 'Filesystem path to mount the extra app volume. Ignored if \'AppVolumeDevice\' is blank')
        string(name: 'AppVolumeSize', defaultValue: '20', description: 'Size in GB of the EBS volume to create. Ignored if \'AppVolumeDevice\' is blank')
        string(name: 'AppVolumeType', defaultValue: 'gp2', description: 'Type of EBS volume to create. Ignored if \'AppVolumeDevice\' is blank')
        string(name: 'BackendTimeout', defaultValue: '600', description: 'How long - in seconds - back-end connection may be idle before attempting session-cleanup')
        string(name: 'BackupBucket', description: 'S3 Bucket to host backups of Jenkins config-data (Optional - if left blank, a value will be computed)')
        string(name: 'BackupFolder', description: 'Folder in S3 Bucket to host backups of Jenkins config-data')
        string(name: 'BucketTemplate', description: 'URL to the child-template for creating the Jenkins S3 backup-bucket.')
        string(name: 'CfnBootstrapUtilsUrl', defaultValue: 'https://s3.amazonaws.com/cloudformation-examples/aws-cfn-bootstrap-latest.tar.gz', description: 'URL to aws-cfn-bootstrap-latest.tar.gz')
        string(name: 'CfnEndpointUrl', defaultValue: 'https://cloudformation.us-east-1.amazonaws.com', description: '(Optional) URL to the CloudFormation Endpoint. e.g. https://cloudformation.us-east-1.amazonaws.com')
        string(name: 'CfnGetPipUrl', defaultValue: 'https://bootstrap.pypa.io/2.6/get-pip.py', description: 'URL to get-pip.py')
        string(name: 'CloudwatchBucketName', description: 'Name of the S3 Bucket hosting the CloudWatch agent archive files')
        string(name: 'CloudWatchAgentUrl', defaultValue: '', description: '(Optional) S3 URL to CloudWatch Agent installer. Example: s3://amazoncloudwatch-agent/linux/amd64/latest/AmazonCloudWatchAgent.zip')
        string(name: 'Ec2Template', description: 'URL to the child-template for creating the Jenkins master node.')
        string(name: 'ElbTemplate', description: 'URL to the child-template for creating the Jenkins ELB.')
        string(name: 'EpelRepo', defaultValue: 'epel', description: 'Name of network's EPEL repo.')
        string(name: 'HaSubnets', description: 'Select three subnets - each from different Availability Zones.')
        string(name: 'IamRoleTemplate', description: 'URL to the child-template for creating the Jenkins IAM instance role.')
        string(name: 'InstanceType', defaultValue: 't2.xlarge', description: 'Amazon EC2 instance type')
        string(name: 'JenkinsAgentPort', description: 'TCP Port number that the Jenkins agent-hosts connect through.')
        string(name: 'JenkinsAppinstallScriptUrl', description: 'URL of Jenkins application-installer script.')
        string(name: 'JenkinsListenPort', defaultValue: '443', description: 'TCP Port number that the Jenkins ELB forwards from.')
        string(name: 'JenkinsListenerCert', defaultValue: '', description: 'Name of ACM-managed SSL Certificate to protect public listener.')
        string(name: 'JenkinsOsPrepScriptUrl', description: 'URL of OS-preparation script.')
        string(name: 'JenkinsPassesSsh', defaultValue: 'false', description: 'Whether to allow SSH passthrough to Jenkins master.')
        string(name: 'JenkinsRepoKeyURL', defaultValue: 'https://pkg.jenkins.io/redhat-stable/jenkins.io.key', description: 'URL to the Jenkins yum-repository GPG key')
        string(name: 'JenkinsRepoURL', defaultValue: 'http://pkg.jenkins.io/redhat-stable', description: 'URL to the Jenkins yum-repository')
        string(name: 'JenkinsRpmName', description: 'Name of Jenkins RPM to install. Include release version if 'other-than-latest' is desired. Example values would be: jenkins, jenkins-2.*, jenkins-X.Y.Z, etc.')
        string(name: 'JenkinsServicePort', defaultValue: '8080', description: 'TCP Port number that the Jenkins host forwards to.')
        string(name: 'KeyPairName', description: 'Public/private key pair allowing an operator to securely connect to instance immediately after instance's SSHD comes online')
        string(name: 'NoPublicIp', defaultValue: 'true', description: 'Controls whether to assign the instance a public IP. Recommended to leave at \'true\' _unless_ launching in a public subnet')
        string(name: 'NoReboot', defaultValue: 'false', description: 'Controls whether to reboot the instance as the last step of cfn-init execution')
        string(name: 'NoUpdates', defaultValue: 'false', description: 'Controls whether to run yum update during a stack update (on the initial instance launch, SystemPrep _always_ installs updates)')
        string(name: 'ProvisionUser', defaultValue: 'autojenk', description: 'Default login user account name.')
        string(name: 'ProxyPrettyName', description: 'A short, human-friendly label to assign to the ELB (no capital letters).')
        string(name: 'PypiIndexUrl', defaultValue: 'https://pypi.org/simple', description: 'URL to the PyPi Index')
        string(name: 'RolePrefix', description: 'Prefix to apply to IAM role to make things a bit prettier (optional).')
        string(name: 'RootVolumeSize', defaultValue: '20', description: 'Size in GB of the EBS volume to create. If smaller than AMI defaul, create operation will fail; If larger, root device-volume's partition size will be increased')
        string(name: 'SecurityGroupTemplate', description: 'URL to the child-template for creating the Jenkins network security-groups.')
        string(name: 'SubnetId', description: 'Subnet to associate to the Instance')
        string(name: 'TargetVPC', description: 'ID of the VPC to deploy Jenkins components into.')
        string(name: 'ToggleCfnInitUpdate', defaultValue: 'A', description: 'A/B toggle that forces a change to instance metadata, triggering the cfn-init update sequence')
        string(name: 'WatchmakerAdminGroups', defaultValue: '', description: '(Optional) Colon-separated list of domain groups that should have admin permissions on the EC2 instance')
        string(name: 'WatchmakerAdminUsers', defaultValue: '', description: '(Optional) Colon-separated list of domain users that should have admin permissions on the EC2 instance')
        string(name: 'WatchmakerComputerName', defaultValue: '', description: '(Optional) Sets the hostname/computername within the OS')
        string(name: 'WatchmakerConfig', defaultValue: '', description: '(Optional) Path to a Watchmaker config file.  The config file path can be a remote source (i.e. http[s]://, s3://) or local directory (i.e. file://)')
        string(name: 'WatchmakerEnvironment', defaultValue: '', description: 'Environment in which the instance is being deployed')
        string(name: 'WatchmakerOuPath', defaultValue: '', description: '(Optional) DN of the OU to place the instance when joining a domain. If blank and \'WatchmakerEnvironment\' enforces a domain join, the instance will be placed in a default container. Leave blank if not joining a domain, or if \'WatchmakerEnvironment\' is \'false\'')
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
                        echo "Attempting to delete any active ${CfnStackRoot}-ParInst stacks... "
                        aws --region "${AwsRegion}" cloudformation delete-stack --stack-name "${CfnStackRoot}-ParInst"

                        aws cloudformation wait stack-delete-complete --stack-name ${CfnStackRoot}-ParInst --region ${AwsRegion}
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
                        echo "Attempting to create stack ${CfnStackRoot}-ParInst..."
                        aws --region "${AwsRegion}" cloudformation create-stack --stack-name "${CfnStackRoot}-ParInst" \
                          --disable-rollback --capabilities CAPABILITY_NAMED_IAM \
                          --template-url "${TemplateUrl}" \
                          --parameters file://parent.instance.parms.json

                        aws cloudformation wait stack-create-complete --stack-name ${CfnStackRoot}-ParInst --region ${AwsRegion}
                    '''
                }
            }
        }
    }
}

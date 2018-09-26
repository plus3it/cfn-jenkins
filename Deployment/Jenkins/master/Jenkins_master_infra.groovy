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
        string(name: 'BackupBucketName', description: 'Logical name of the S3 Bucket used to host Jenkins backups')
        string(name: 'BucketTemplate', description: 'URL to the child-template for creating the Jenkins S3 backup-bucket.')
        string(name: 'CloudwatchBucketName', description: 'Name of the S3 Bucket hosting the CloudWatch agent archive files')
        string(name: 'IamRoleTemplate', description: 'URL to the child-template for creating the Jenkins IAM instance role.')
        string(name: 'JenkinsAgentPort', description: 'TCP Port number that the Jenkins agent-hosts connect through.')
        string(name: 'RolePrefix', description: 'Prefix to apply to IAM role to make things a bit prettier (optional).')
        string(name: 'SecurityGroupTemplate', description: 'URL to the child-template for creating the Jenkins network security-groups.')
        string(name: 'ServiceTld', defaultValue: 'amazonaws.com', description: 'TLD of the IAMable service-name.')
        string(name: 'TargetVPC', description: 'ID of the VPC to deploy Jenkins components into.')
    }

    stages {
        stage ('Prepare Agent Environment') {
            steps {
                deleteDir()
                git branch: "${GitProjBranch}",
                    credentialsId: "${GitCred}",
                    url: "${GitProjUrl}"
                writeFile file: 'service-infra.parms.json',
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
                                "ParameterKey": "BackendTimeout",
                                "ParameterValue": "${env.BackendTimeout}"
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
                                "ParameterKey": "BucketTemplate",
                                "ParameterValue": "${env.BucketTemplate}"
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
                                "ParameterKey": "CloudwatchBucketName",
                                "ParameterValue": "${env.CloudwatchBucketName}"
                            },
                            {
                                "ParameterKey": "CloudWatchAgentUrl",
                                "ParameterValue": "${env.CloudWatchAgentUrl}"
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
                                "ParameterKey": "EpelRepo",
                                "ParameterValue": "${env.EpelRepo}"
                            },
                            {
                                "ParameterKey": "HaSubnets",
                                "ParameterValue": "${env.HaSubnets}"
                            },
                            {
                                "ParameterKey": "IamRoleTemplate",
                                "ParameterValue": "${env.IamRoleTemplate}"
                            },
                            {
                                "ParameterKey": "InstanceType",
                                "ParameterValue": "${env.InstanceType}"
                            },
                            {
                                "ParameterKey": "JenkinsAgentPort",
                                "ParameterValue": "${env.JenkinsAgentPort}"
                            },
                            {
                                "ParameterKey": "JenkinsAppinstallScriptUrl",
                                "ParameterValue": "${env.JenkinsAppinstallScriptUrl}"
                            },
                            {
                                "ParameterKey": "JenkinsListenPort",
                                "ParameterValue": "${env.JenkinsListenPort}"
                            },
                            {
                                "ParameterKey": "JenkinsListenerCert",
                                "ParameterValue": "${env.JenkinsListenerCert}"
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
                                "ParameterKey": "JenkinsRepoURL",
                                "ParameterValue": "${env.JenkinsRepoURL}"
                            },
                            {
                                "ParameterKey": "JenkinsRpmName",
                                "ParameterValue": "${env.JenkinsRpmName}"
                            },
                            {
                                "ParameterKey": "JenkinsServicePort",
                                "ParameterValue": "${env.JenkinsServicePort}"
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
                                "ParameterKey": "ProvisionUser",
                                "ParameterValue": "${env.ProvisionUser}"
                            },
                            {
                                "ParameterKey": "ProxyPrettyName",
                                "ParameterValue": "${env.ProxyPrettyName}"
                            },
                            {
                                "ParameterKey": "PypiIndexUrl",
                                "ParameterValue": "${env.PypiIndexUrl}"
                            },
                            {
                                "ParameterKey": "RolePrefix",
                                "ParameterValue": "${env.RolePrefix}"
                            },
                            {
                                "ParameterKey": "RootVolumeSize",
                                "ParameterValue": "${env.RootVolumeSize}"
                            },
                            {
                                "ParameterKey": "SecurityGroupTemplate",
                                "ParameterValue": "${env.SecurityGroupTemplate}"
                            },
                            {
                                "ParameterKey": "SubnetId",
                                "ParameterValue": "${env.SubnetId}"
                            },
                            {
                                "ParameterKey": "TargetVPC",
                                "ParameterValue": "${env.TargetVPC}"
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
                        echo "Attempting to delete any active ${CfnStackRoot}-Infra-${BUILD_NUMBER} stacks... "
                        aws --region "${AwsRegion}" cloudformation delete-stack --stack-name "${CfnStackRoot}-Infra-${BUILD_NUMBER}"

                        aws cloudformation wait stack-delete-complete --stack-name ${CfnStackRoot}-Infra-${BUILD_NUMBER} --region ${AwsRegion}
                    '''
                }
            }
        }
        stage ('Launch Jenkins Master Parent Instance Stack') {
            steps {
                withCredentials(
                    [
                        [$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: "${AwsCred}", secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'],
                        sshUserPrivateKey(credentialsId: "${GitCred}", keyFileVariable: 'SSH_KEY_FILE', passphraseVariable: 'SSH_KEY_PASS', usernameVariable: 'SSH_KEY_USER')
                    ]
                ) {
                    sh '''#!/bin/bash
                        echo "Attempting to create stack ${CfnStackRoot}-Infra-${BUILD_NUMBER}..."
                        aws --region "${AwsRegion}" cloudformation create-stack --stack-name "${CfnStackRoot}-Infra-${BUILD_NUMBER}" \
                          --disable-rollback --capabilities CAPABILITY_NAMED_IAM \
                          --template-body file://Templates/make_jenkins_infra.tmplt.json \
                          --parameters file://service-infra.parms.json

                        aws cloudformation wait stack-create-complete --stack-name ${CfnStackRoot}-Infra-${BUILD_NUMBER} --region ${AwsRegion}
                    '''
                }
            }
        }
    }
}

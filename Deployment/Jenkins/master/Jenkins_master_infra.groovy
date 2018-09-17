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
        string(name: 'BucketTemplate', description: 'link to bucket template')
        string(name: 'IamRoleTemplate', description: 'Link to IAM template')
        string(name: 'SecurityGroupTemplate', description: 'Link to SG template')
        string(name: 'ServiceTld', defaultValue: 'amazonaws.com', description: 'TLD of the IAMable service-name')
        string(name: 'JenkinsBackupBucket', description: '(Optional: will be randomly named if left un-set) Name to give to S3 Bucket used for longer-term retention of backups')
        string(name: 'RolePrefix', description: '(Optional) Prefix to apply to IAM role')
        string(name: 'TargetVPC', description: 'ID of the VPC to deploy cluster nodes into')
        string(name: 'JenkinsAgentPort', defaultValue: '', description: 'TCP Port number that the Jenkins agent-hosts connect through')
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
                            "ParameterKey": "BucketTemplate",
                            "ParameterValue": "${env.BucketTemplate}"
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
                            "ParameterKey": "JenkinsAgentPort",
                            "ParameterValue": "${env.JenkinsAgentPort}"
                        },
                        {
                            "ParameterKey": "JenkinsBackupBucket",
                            "ParameterValue": "${env.JenkinsBackupBucket}"
                        },
                        {
                            "ParameterKey": "RolePrefix",
                            "ParameterValue": "${env.RolePrefix}"
                        },
                        {
                            "ParameterKey": "TargetVPC",
                            "ParameterValue": "${env.TargetVPC}"
                        },
                        {
                            "ParameterKey": "ServiceTld",
                            "ParameterValue": "${env.ServiceTld}"
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

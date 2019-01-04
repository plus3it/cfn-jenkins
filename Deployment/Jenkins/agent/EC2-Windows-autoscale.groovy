pipeline {

    agent any

    options {
        buildDiscarder(
            logRotator(
                numToKeepStr: '10',
                daysToKeepStr: '30',
                artifactDaysToKeepStr: '30',
                artifactNumToKeepStr: '10'
            )
        )
        disableConcurrentBuilds()
        timeout(time: 90, unit: 'MINUTES')
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
        string(name: 'AppScriptUrl', description: 'Optional) S3 URL to the .ps1 or .bat application script in an S3 bucket (s3://). Leave blank to launch without an application script. If specified, an appropriate InstanceRole is required')
        string(name: 'AppScriptParams', description: 'Parameter string to pass to the application script. Ignored if AppScriptUrl is blank')
        string(name: 'AmiId', description: 'ID of the AMI to launch')
        string(name: 'AppVolumeDevice', defaultValue: 'false', description: 'Whether to mount an extra EBS volume. Leave as default (\"false\") to launch without an extra application volume')
        string(name: 'AppVolumeSize', defaultValue: '20', description: 'Size in GB of the EBS volume to create. Ignored if \"AppVolumeDevice\" is blank')
        string(name: 'AppVolumeType', defaultValue: 'gp2', description: 'Type of EBS volume to create. Ignored if \"AppVolumeDevice\" is blank')
        string(name: 'ChainScriptUrl', description: 'The URL of the chain load script')
        string(name: 'ChainScriptParams', defaultValue: '', description: 'Parameters to pass to chain load')
        string(name: 'CfnEndpointUrl', defaultValue: 'https://cloudformation.us-east-1.amazonaws.com', description: 'URL of the Cloudformation endpoint')
        string(name: 'DesiredCapacity', defaultValue: '1', description: 'Desired number of instances in auto scaling group')
        string(name: 'InstanceRole', defaultValue: '', description: 'The IAM role to apply to the instance')
        string(name: 'InstanceType', defaultValue: 't2.small', description: 'Amazon EC2 instance type')
        string(name: 'KeyPairName', description: 'Public/private key pairs allow you to securely connect to your instance after it launches')
        string(name: 'MaxCapacity', defaultValue: '2', description: 'Max number of instances in auto scaling group')
        string(name: 'MinCapacity', defaultValue: '1', description: 'Min number of instances in auto scaling group')
        string(name: 'NoPublicIp', defaultValue: 'true', description: 'Controls whether to assign the instance a public IP. Recommended to leave at \"true\" _unless_ launching in a public subnet')
        string(name: 'NoReboot', defaultValue: 'false', description: 'Controls whether to reboot the instance as the last step of cfn-init execution')
        string(name: 'NpmSetupUri', defaultValue: 'https://rpm.nodesource.com/setup_8.x', description: 'URI of node.js NPM setup')
        string(name: 'PrivateIp', defaultValue: '', description: '(Optional) Set a static, primary private IP. Leave blank to auto-select a free IP')
        string(name: 'PypiIndexUrl', defaultValue: 'https://pypi.org/simple', description: 'URL for pypi')
        string(name: 'SecurityGroupIds', description: 'The security groups to apply to the instance')
        string(name: 'SubnetIds', description: 'specific subnets to deploy into')
    }

    stages {
        stage ('Prepare Agent Environment') {
            steps {
                deleteDir()
                git branch: "${GitProjBranch}",
                    credentialsId: "${GitCred}",
                    url: "${GitProjUrl}"
                writeFile file: 'InfraStack.parms.json',
                    text: /
                    [
                      {
                        "ParameterKey": "AmiId",
                        "ParameterValue": "${env.AmiId}"
                      },
                      {
                        "ParameterKey": "AppVolumeDevice",
                        "ParameterValue": "${env.AppVolumeDevice}"
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
                        "ParameterKey": "AppScriptUrl",
                        "ParameterValue": "${env.AppScriptUrl}"
                      },
                      {
                        "ParameterKey": "AppScriptParams",
                        "ParameterValue": "${env.AppScriptParams}"
                      },
                      {
                        "ParameterKey": "ChainScriptUrl",
                        "ParameterValue": "${env.ChainScriptUrl}"
                      },
                      {
                        "ParameterKey": "ChainScriptParams",
                        "ParameterValue": "${env.ChainScriptParams}"
                      },
                      {
                        "ParameterKey": "DesiredCapacity",
                        "ParameterValue": "${env.DesiredCapacity}"
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
                        "ParameterKey": "KeyPairName",
                        "ParameterValue": "${env.KeyPairName}"
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
                        "ParameterKey": "PrivateIp",
                        "ParameterValue": "${env.PrivateIp}"
                      },
                      {
                      "ParameterKey": "SecurityGroupIds",
                      "ParameterValue": "${env.SecurityGroupIds}"
                      },
                      {
                      "ParameterKey": "SubnetIds",
                      "ParameterValue": "${env.SubnetIds}"
                      }
                    ]
                   /
                }
            }
        stage ('Prepare AWS Environment') {
            options {
                timeout(time: 2, unit: 'HOURS')
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

                        sleep 5

                        # Pause if delete is slow
                        while [[ $(
                                    aws cloudformation describe-stacks \
                                      --stack-name ${CfnStackRoot} \
                                      --query 'Stacks[].{Status:StackStatus}' \
                                      --out text 2> /dev/null | \
                                    grep -q DELETE_IN_PROGRESS
                                   )$? -eq 0 ]]
                        do
                           echo "Waiting for stack ${CfnStackRoot} to delete..."
                           sleep 30
                        done
                    '''
                }
            }
        }
        stage ('Launch Jenkins Master Stack') {
            options {
                timeout(time: 2, unit: 'HOURS')
            }
            steps {
                withCredentials(
                    [
                        [$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: "${AwsCred}", secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'],
                        sshUserPrivateKey(credentialsId: "${GitCred}", keyFileVariable: 'SSH_KEY_FILE', passphraseVariable: 'SSH_KEY_PASS', usernameVariable: 'SSH_KEY_USER')
                    ]
                ) {
                    sh '''#!/bin/bash
                        echo "Attempting to create stack ${CfnStackRoot}..."
                        aws --region "${AwsRegion}" cloudformation create-stack --stack-name "${CfnStackRoot}" \
                          --disable-rollback --capabilities CAPABILITY_NAMED_IAM \
                          --template-url "${PrimaryTemplate}" \
                          --parameters file://InfraStack.parms.json

                        sleep 15

                        # Pause if create is slow
                        while [[ $(
                                    aws cloudformation describe-stacks \
                                      --stack-name ${CfnStackRoot} \
                                      --query 'Stacks[].{Status:StackStatus}' \
                                      --out text 2> /dev/null | \
                                    grep -q CREATE_IN_PROGRESS
                                   )$? -eq 0 ]]
                        do
                           echo "Waiting for stack ${CfnStackRoot} to finish create process..."
                           sleep 30
                        done

                        if [[ $(
                                aws cloudformation describe-stacks \
                                  --stack-name ${CfnStackRoot} \
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

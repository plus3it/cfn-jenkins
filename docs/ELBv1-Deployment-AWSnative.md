## Deploying ("Classic") Elastic LoadBalancers Template Using Only AWS Native Tools

This document describes how to launch the ("Classic") Elastic LoadBalancer template using only native AWS tools

### AWS CLI

The AWS CLI can be used to launch the template using either a locally-staged copy of the template or an S3-hosted copy of the template. In either case it will be necessary to supply template-parameters. While this can be done directly within the CLI, readability and repeatability are aided by using a parameters file. The parameters file will resemble the following:

~~~~
[
    {
        "ParameterKey": "BackendTimeout",
        "ParameterValue": "600"
    },
    {
        "ParameterKey": "ProxyPrettyName",
        "ParameterValue": "jenkins"
    },
    {
        "ParameterKey": "JenkinsAgentPort",
        "ParameterValue": "37312"
    },
    {
        "ParameterKey": "JenkinsListenerCert",
        "ParameterValue": "cc2f9d11-8a4f-4826-b9f4-00c19a5281cc"
    },
    {
        "ParameterKey": "HaSubnets",
        "ParameterValue": "subnet-3ec53d71,subnet-af614311,subnet-fef53107"
    },
    {
        "ParameterKey": "JenkinsListenPort",
        "ParameterValue": "443"
    },
    {
        "ParameterKey": "JenkinsServicePort",
        "ParameterValue": "8080"
    },
    {
        "ParameterKey": "JenkinsPassesSsh",
        "ParameterValue": "false"
    },
    {
        "ParameterKey": "SecurityGroupIds",
        "ParameterValue": "sg-cd02a1b00ef13c71a"
    }
]
~~~~

Done directly within the cli, the required parameters-enumeration would look like:

~~~~
--parameters '[{"ParameterKey":"BackendTimeout","ParameterValue":"600"},{"ParameterKey":"ProxyPrettyName","ParameterValue":"jenkins"},{"ParameterKey":"JenkinsAgentPort","ParameterValue":"37312"},{"ParameterKey":"JenkinsListenerCert","ParameterValue":"cc2f9d11-8a4f-4826-b9f4-00c19a5281cc"},{"ParameterKey":"HaSubnets","ParameterValue":"subnet-3ec53d71,subnet-af614311,subnet-fef53107"},{"ParameterKey":"JenkinsListenPort","ParameterValue":"443"},{"ParameterKey":"JenkinsServicePort","ParameterValue":"8080"},{"ParameterKey":"JenkinsPassesSsh","ParameterValue":"false"},{"ParameterKey":"SecurityGroupIds","ParameterValue":"sg-cd02a1b00ef13c71a"}]'
~~~~

With the relative complexity of the ("Classic") Elastic LoadBalancer template, specifying all the parameters directly on the commandline becomes fairly horrendous. It's _strongly_ recommended to use a parameters-file when deploying from the ELB template.

The following sub-sections assume:
- The template user has previously used `aws configure` to set a default region for their commands to run within. If this is not the case, it will be necessary to insert the `--region <REGION_NAME>` option to the CLI command
- The template user is not attempting to execute the command against a non-default account-profile. If this is not the case, it will be necessary to insert the `--profile <PROFILE_NAME>` option to the CLI command

#### Locally Hosted Template

When referencing a locally-hosted file, one uses the `--template-body` parameter. Depending on the OS type being launched from specifying the path to the template may require using the `file://` URI. Similarly, if using a parameters file, one may need to use the `file://` URI. The resulting command will look like:

~~~~
aws cloudformation create-stack --stack-name make_jenkins_ELBv1-pub-autoscale.tmplt.json \
  --template-body file:///<FULLY_QUALIFIED>/<PATH>/<TO>/<TEMPLATE> \
  --parameters file:///<FULLY_QUALIFIED>/<PATH>/<TO>/<PARAMETER_FILE>
~~~~

Or:

~~~~
aws cloudformation create-stack --stack-name make_jenkins_ELBv1-pub-autoscale.tmplt.json \
  --template-body file://<RELATIVE>/<PATH>/<TO>/<TEMPLATE> \
  --parameters file://<RELATIVE>/<PATH>/<TO>/<PARAMETER_FILE>
~~~~

The latter saves typing if the template- and parameter-files are either co-located or proximal to each other within the local filesystem. This can mean the difference between typing:

`file:///home/myuser/GIT/cfn-jenkins/Templates/make_jenkins_ELBv1-pub-autoscale.tmplt.json`

and typing:

`file://make_jenkins_ELBv1-pub-autoscale.tmplt.json`

#### S3-Hosted Template

When referencing a S3-hosted file, one uses the `--template-url` parameter. If using a parameters file, one may need to use the `file://` URI. The resulting command will look like:

~~~~
aws cloudformation create-stack --stack-name make_jenkins_ELBv1-pub-autoscale.tmplt.json \
  --template-url https://s3.<REGION>.<AMAZON_TLD>/<BUCKET_NAME>/<TEMPLATE_NAME> \
  --parameters file:///<FULLY_QUALIFIED>/<PATH>/<TO>/<PARAMETER_FILE>
~~~~

Or:

~~~~
aws cloudformation create-stack --stack-name make_jenkins_ELBv1-pub-autoscale.tmplt.json \
  --template-url https://s3.<REGION>.<AMAZON_TLD>/<BUCKET_NAME>/<TEMPLATE_NAME> \
  --parameters file://<RELATIVE>/<PATH>/<TO>/<PARAMETER_FILE>
~~~~

### AWS Web UI

(TBD)


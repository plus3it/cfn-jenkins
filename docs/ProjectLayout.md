## Project Layout

The functional content for this project is conisists of three main categories. Each functional-category is reflected as its own directory hierarchy.

### Templates

This project's primary aim is to automate the deployment of the Jenkins service within AWS via [ClouFormation](https://aws.amazon.com/cloudformation/aws-cloudformation-templates/) (CFn templates). Therefore, the template-directory would be considered the core of this project. Each template file &mdash; with the exception of the "parent" templates &mdash; corresponds to a discrete set of related AWS deployment tasks. Templates are named to try to reflect the AWS configuration elements or processes being automated:

#### Service Templates

The following templates underpin the automation of deploying AWS service elements:

* [`make_jenkins_EC2-Master-autoscale.tmplt.json`](/Templates/make_jenkins_EC2-Master-autoscale.tmplt.json): Deploys an auto-scaled, STIG-hardened, EL7-based [EC2 instance](https://aws.amazon.com/ec2/) to host a Jenkins master-node. AWS [AutoScaling service](https://aws.amazon.com/autoscaling/) improves availability of the master-node service.
* [`make_jenkins_EC2-Master-instance.tmplt.json`](/Templates/make_jenkins_EC2-Master-instance.tmplt.json): Deploys a STIG-hardened, EL7-based EC2 instance to host a Jenkins master-node.
* [`make_jenkins_EC2-Agent-Linux-autoscale.tmplt.json`](/Templates/make_jenkins_EC2-Agent-Linux-autoscale.tmplt.json): Deploys an auto-scaled, STIG-hardened, EL7-based EC2 instance to host a Jenkins agent-node. AWS AutoScaling service improves availability of the agent-node service.
* [`make_jenkins_EC2-Agent-Linux-instance.tmplt.json`](/Templates/make_jenkins_EC2-Agent-Linux-instance.tmplt.json): Deploys a STIG-hardened, EL7-based EC2 instance to host a Jenkins agent-node:
* [`make_jenkins_ELBv1-pub-autoscale.tmplt.json`](/Templates/make_jenkins_ELBv1-pub-autoscale.tmplt.json): Deploys a user-facing, "[Classic](https://docs.aws.amazon.com/elasticloadbalancing/latest/classic/introduction.html)" [Elastic Loadbalancer](https://docs.aws.amazon.com/elasticloadbalancing/index.html).
* [`make_jenkins_ELBv2-pub.tmplt.json`](/Templates/make_jenkins_ELBv2-pub.tmplt.json): Deploys a user-facing, "[Application](https://docs.aws.amazon.com/elasticloadbalancing/latest/application/introduction.html)" Elastic Loadbalancer. _Note_: This template is not currently well-maintained and does not come with any Jenkins pipeline-definition.
* [`make_jenkins_IAM-role.tmplt.json`](/Templates/make_jenkins_IAM-role.tmplt.json): Creates an [IAM instance-role](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/iam-roles-for-amazon-ec2.html). When attached to an EC2 instance, provides access to a deployment's S3-based resources as well as providing access to cloud-layer functionality required to enable centralized management (via [SSM]()https://docs.aws.amazon.com/systems-manager/index.html), logging (via [CloudWatch](https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/WhatIsCloudWatchLogs.html)).
* [`make_jenkins_S3-bucket.tmplt.json`](/Templates/make_jenkins_S3-bucket.tmplt.json): Creates an [S3](https://aws.amazon.com/s3/) [bucket](https://docs.aws.amazon.com/AmazonS3/latest/dev/UsingBucket.html) used (primarily) to host the Jenkins service's daily and [auto-recovery](/docs/AutoRecovery.md) backups. Buckets are created with a default security-posture of "private". Access to bucket-contents are only available to authenticated users and processes. Process-accesses are authenticated by way of the previously-mentioned attachment of IAM instance-roles to the Jenkins EC2 instances.
* [`make_jenkins_SGs.tmplt.json`](/Templates/make_jenkins_SGs.tmplt.json): Creates the VPC [security-groups](https://docs.aws.amazon.com/vpc/latest/userguide/VPC_SecurityGroups.html) used to protect AWS-hosted Jenkins resources. It is expected that Jenkins-hosting EC2 instances will be deployed to unroutable subnets and that any resources that need user-facing access will be routed to the EC2s by way of user-facing proxies (see the previously-noted Elastic Load-balancers). This template creates the requisite network permissions for:
    * Users to access the web-proxy service-components
    * The access-proxy to bidirectionally communicate with the EC2 hosting the Jenkins Master service
    * The Jenkins Master service and Jenkins Agent-nodes to communicate with each other.

#### Parent/Control Templates

The following templates act as control-layers for the [service-templates](/docs/ProjectLayout.md#service-templates). The service-templates deploy in related "layers", with higher-level layers depending on the lower-level layers being in a good/complete state. The lower-level layers export configuration-information used by the higher-level layers. The "parent" stacks provide automatic linking of lower- and higher-level layers by using the lower-level layers' outputs as inputs to the higher-level layers. This eliminates the need to manually-populate each of the service-template's inputs.

* [`make_jenkins_infra.tmplt.json`](/Templates/make_jenkins_infra.tmplt.json): This template is used to control the deployment of `SG`, `S3` and `IAM` service elements. These elements are then leveraged by the `ELB`, `Master` and `Agent` templates to provide services to the Jenkins users.
* [`make_jenkins_parent-instance.tmplt.json`](/Templates/make_jenkins_parent-instance.tmplt.json): This is an "end-to-end" service-deployment template. It coordinates the invocation of the `SG`, `S3`, `IAM`, `ELB` and autoscaling master `EC2` templates. When the parent stack completes, a fully-instantiated Jenkins service should be available and ready for configuration (initial configuration of the application and setting up appropriate DNS entries to point Jenkins-users at the service-proxy)
* [`make_jenkins_parent-autoscale.tmplt.json`](/Templates/make_jenkins_parent-autoscale.tmplt.json): This is an "end-to-end" service-deployment template. It coordinates the invocation of the `SG`, `S3`, `IAM`, `ELB` and autoscaling master `EC2` templates. When the parent stack completes, a fully-instantiated Jenkins service - with enhanced service-availability via AutoScaling - should be available and ready for configuration (initial configuration of the application and setting up appropriate DNS entries to point Jenkins-users at the service-proxy)

### SupportFiles

This directory contains the files invoked by the EC2 templates to handle the configuration of the deployed EC2 instances to host Jenkins Master and Agent services.

* [`jenkins_osprep.sh`](/SupportFiles/jenkins_osprep.sh): This script takes care of the baseline configuration of the underlying EL7 host. This script makes the host ready for the installation of the Jenkins Master service.
* [`jenkins_appinstall.sh`](/SupportFiles/jenkins_appinstall.sh): This script installs the Jenkins Master service. This script detects whether the installation should proceed as a "fresh" install or as an "auto-recovery" install:
    * In the former case, upon completion, the Jenkins Master service will be ready for initial configuration.
    * In the latter case, upon completion, the Jenkins Master service will be pre-configured to the state defined in the auto-recovery files.
* [`jenkins-agent_userscript.sh`](/SupportFiles/jenkins-agent_userscript.sh): Simple script to `curl` the `agent.jar` file from the Jenkins Master node and install it.
* `jenkins-agent_userscript.ps1`: Content TBD: authoring in progress
* [`chain-load.sh`](/SupportFiles/chain-load.sh): This script automates the tailoring of an Agent-node to meet the template user's specific needs. Script will mostly be used for pre-installing Jenkins agent plugins and doing further configuration of the agent-node to meet the needs of those plugins. Notes:
    * The script hosted in this repository is meant to act as an example of the types of things that _can_ be done.
    * Template-users' _real_ chain-scripts should be hosted in a protected, `curl`-fetchable repository service (e.g. Artifactory, authenticated HTTP download directory, etc.) and maintained in a separate, version-controlled, _private_ projects.
    * This document's emphasis on protecting the template-users' "real" script is due to the expectation that such script may contain potentially sensitive information (API tokens or other credentials; back-end system-names/addresses; etc).

### Deployment

This directory hierarchy is designed to contain tools to further facilitate deployments leveraging other automation frameworks.

_Note: Currently, this hierarchy only contains Jenkins pipeline definitions (in the `Jenkins` subdirectory). As this project is matured, it is expected that support for other frameworks will be added. Users of this project should feel free to contribute support for further frameworks._

#### Jenkins

This directory (hierarchy) contains Jenkins pipeline definitions to allow placing the CFn-based service-deployment components under the control of a Jenkins domain.

##### Master

This directory contains the Jenkins pipeline definitions used to place the CFn-templates used for deploying Jenkins Master server elements under Jenkins control:

###### Template-drivers:

* [`EC2-Autoscale.groovy`](/Deployment/Jenkins/master/EC2-Autoscale.groovy): Jenkins pipeline-definition to drive the `make_jenkins_EC2-Master-Linux-autoscale.tmplt.json` CFn template.
* [`EC2-Instance.groovy`](/Deployment/Jenkins/master/EC2-Instance.groovy): Jenkins pipeline-definition to drive the `make_jenkins_EC2-Master-Linux-instance.tmplt.json` CFn template.
* [`Infra-layers.groovy`](/Deployment/Jenkins/master/Infra-layers.groovy): Jenkins pipeline-definition to drive the `make_jenkins_infra.tmplt.json` CFn template.
* [`Master-Elbv1.groovy`](/Deployment/Jenkins/master/Master-Elbv1.groovy): Jenkins pipeline-definition to drive the `make_jenkins_ELBv1-pub-autoscale.tmplt.json` CFn template.
* [`Parent-Full-Instance.groovy`](/Deployment/Jenkins/master/Parent-Full-Instance.groovy): Jenkins pipeline-definition to drive the `make_jenkins_parent-instance.tmplt.json` CFn template.

###### "Helpers":

* [`S3-MigrationHelper.groovy`](/Deployment/Jenkins/master/S3-MigrationHelper.groovy): Jenkins pipeline-definition to facilitate the migration of backup data from an previously-existing S3 bucket to a new/empty S3 bucket

##### Agent

This directory contains the Jenkins pipeline definitions used to place the CFn-templates used for deploying Jenkins Agent server elements under Jenkins control:

###### Template-drivers:

* [`agent-instance.groovy`](/Deployment/Jenkins/agent/agent-instance.groovy): Jenkins pipeline-definition to drive the `make_jenkins_EC2-Agent-Linux-instance.tmplt.json` CFn template.


###### "Helpers":

Currently, there are not any "helper" definitions for Agent nodes.

#### TBD

(This section left intentionally blank)

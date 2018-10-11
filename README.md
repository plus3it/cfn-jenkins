# About

The Jenkins project is a sub-project of our overarching DevOps Tool-Chain (DOTC) project. This project &mdash; and its peer projects &mdash; is designed to handle the automated deployment of common DevOps tool-chain services onto STIG-harderend, EL7-compatible Amazon EC2 instances and related AWS resources. Specifically, this project provides CloudFormation (CFn) templates for:

* Security Groups
* Simple Storage Service (S3)
* Identity and Access Management (IAM)
* "Classic" Elastic LoadBalancer
* Application LoadBalancer (a.k.a., "ELBv2")
* Standalone EC2 Jenkins "Master" instance
* Auto-scaling EC2 Jenkins "Master" instance
* Standalone EC2 "agent" instance
* Auto-scaling EC2 "agent" instance
* "One-button" Master deployment
* "One-button" deployment of Master-supporting AWS elements

Additionally, scripts are provided to automate the deployment of the Jenkins Master and Agent services (see the Jenkins [Distributed Builds](https://wiki.jenkins.io/display/JENKINS/Distributed+builds) guide) onto the relevant EC2 instances - whether stand-alone or managed via AWS's [AutoScaling](https://aws.amazon.com/autoscaling/) service.

## Design Assumption

These templates are intended for use within AWS [VPCs](https://aws.amazon.com/vpc/). It is further expected that the deployed-to VPCs will be configured with public and private subnets. All Jenkins elements other than the Elastic LoadBalancer(s) are expected to be deployed into private subnets. The Elastic LoadBalancers provide transit of Internet-originating web UI requests to the the Jenkins Master node's web-based management-interface.

These templates _may_ work outside of such a networking-topology but have not been so tested.

## Notes on Templates' Usage

It is generally expected that the use of the various, individual-service templates will be run either via the "parent" templates or some other control framework (e.g. Jenkins).

The included "parent" template allows for a kind of "one-button" deployment method, executed with wholly AWS native tools. The "parent" templates attempt to make it so that all the template-user needs to worry about is populating the template's fields and ensuring that CFn can find the child templates. 

In order to use the "parent" template, it is recommended that the child templates be hosted in an S3 bucket separate from the one created for backups by this stack-set. The template-hosting bucket may be public or not. The files may be set to public or not. CFn typically has sufficient privileges to read the templates from a bucket without requiring the containing bucket or files being set public. Use of S3 for hosting eliminates the need to find other hosting-locations or sort out access-particulars of those hosting locations.

The EC2-related templates &mdash; either autoscale or instance &mdash; currently require that the scripts be anonymously curlable. The scripts can still be hosted in a non-public S3 bucket, however, either:
* The scripts' file-ACLs will need to allow `public-read`; or,
* The scripts will need to be referenced by way of [pre-signed URLs](https://docs.aws.amazon.com/AmazonS3/latest/dev/ShareObjectPreSignedURL.html).
This limitation may change in future releases &mdash; likely via an enhancement to the IAM template or fetch-method.

It will be necessary to deploy the Jenkins Master prior to deploying the Jenkins agent node(s). These templates assume the use of JNLP for configuring communications between master and agent nodes. The JNLP connection requires a unique access-token per agent that can only be generated once the master server is online.

These templates (currently) do _not_ include Route53 functionality. It is assumed that the requisite Route53 or other DNS aliases will be configured separate from the instantiation of the public-facing ELB.

* While there are templates provided for standalone Master and Agents, they are provided mostly for "completeness". It's expected most users of these templates will desire the availability-enhancements accorded by the use of the auto-scaling templates.
* The ELBv2 template is similarly provided for "completeness".

## Further Documentation:

See the `docs` subdirectory. It currently contains the following documents:

* [ProjectLayout.md](docs/ProjectLayout.md)
* Automated Deployment with AWS Native Tooling
    * Master Server Elements
        * [SecurityGroup-Deployment-AWSnative.md](docs/SecurityGroup-Deployment-AWSnative.md)
        * [S3-Deployment-AWSnative.md](docs/S3-Deployment-AWSnative.md)
        * [IAM-Deployment-AWSnative.md](docs/IAM-Deployment-AWSnative.md)
        * [ELBv1-Deployment-AWSnative.md](docs/ELBv1-Deployment-AWSnative.md)
        * [Master-Deployment-AWSnative.md](docs/Master-Deployment-AWSnative.md)
        * [OneButtonMasterDeployment-AWSnative.md](docs/OneButtonMasterDeployment-AWSnative.md)
    * Agent Server Elements
        * [Agent-Linux-Deployment-AWSnative.md](docs/Agent-Linux-Deployment-AWSnative.md)
        * [Agent-Windows-Deployment-AWSnative.md](docs/Agent-Windows-Deployment-AWSnative.md)
* Automated Deployment with Jenkins Overlays
    * Master Server Elements
        * [IAM-Deployment-Jenkins.md](docs/IAM-Deployment-Jenkins.md)
        * [ELBv1-Deployment-Jenkins.md](docs/ELBv1-Deployment-Jenkins.md)
        * [Master-Deployment-Jenkins.md](docs/Master-Deployment-Jenkins.md)
        * [S3-Deployment-Jenkins.md](docs/S3-Deployment-Jenkins.md)
        * [OneButtonMasterDeployment-Jenkins.md](docs/OneButtonMasterDeployment-Jenkins.md)
        * [SecurityGroup-Deployment-Jenkins.md](docs/SecurityGroup-Deployment-Jenkins.md)
    * Agent Server Elements
        * [Agent-Linux-Deployment-Jenkins.md](docs/Agent-Linux-Deployment-Jenkins.md)
        * [Agent-Windows-Deployment-Jenkins.md](docs/Agent-Windows-Deployment-Jenkins.md)
* Miscellaneous Topics
    * [AutoRecovery.md](docs/AutoRecovery.md)
    * [ChainLoad.md](docs/ChainLoad.md)

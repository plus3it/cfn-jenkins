# Deploying a Parallel, Upgraded Jenkins Service - Master Node

## Purpose

This document is intended to walk the automation-user through the process of deploying a paralle, upgraded deployment of an existing Jenkis service using templates included in this project.

## Dependencies

Because a parallel-deployment is essentially the same as for a wholly-new deployment, The following dependencies are the same as those for a [brand-new deployment](Deployment-Fresh.md#dependencies):

* Access to an AWS account
* An IAM user or role with (at least) enough permissions to:
    * Create EC2 network security groups
    * Create S3 buckets
    * Create classic Elastic LoadBalancers (ELBs)
    * Create IAM instance-roles and policies
    * Create RDS databases
    * Create EFS shares (Optional: only required if deploying to an EFS-supporting region and choosing to use EFS for persistent storage)
    * Create CloudWatch Logs log-groups (Optional: only required if deploying to a region that supports the use of the CloudWatch Logs service and wishing to take advantage of same)
    * Create new DNS records in Route53 (Optional: only required if deploying to a region that supports Route53 and use of Route53 DNS service is desired)
* Access to a computer that has a modern git client
* Access to a computer that has Python installed (and is pip-enabled)
* Access to a computer that has a modern web browser
* Access to a computer that has the AWS CLI installed or _installable_ (e.g., `pip install awscli`)
* Ability to configure the AWS CLI to use the previously-described IAM user or role
* Availability of a known-good, _recent_ backup of the to-be-replaced Jenkins service.

## Automation Elements

See section-contents in [brand-new deployment](Deployment-Fresh.md#automation-elements)

### Cloud-Level Automation

See section-contents in [brand-new deployment](Deployment-Fresh.md#cloud-level-automation)

#### Directly-Used Templates

See section-contents in [brand-new deployment](Deployment-Fresh.md#directly-used-templates)
 
#### Indirectly-Used Templates

See section-contents in [brand-new deployment](Deployment-Fresh.md#indirectly-used-templates)

### Instance-Level Automation

See section-contents in [brand-new deployment](Deployment-Fresh.md#instance-level-automation)

## Deployment/Workflow

The "upgrade" process follows a generic workflow of:

1. Deploy new "infrastructure" stack-set &mdash; See _Wholly-New Jenkins Service_'s [Cloud Provisioning](Deployment-Fresh.md#cloud-provisioning) section.
1. Deploy new ELB stack &mdash; See _Wholly-New Jenkins Service_'s [Cloud Provisioning](Deployment-Fresh.md#cloud-provisioning) section.
1. Duplicate backup-data from prior Jenkins service's backup-bucket to the new backup-bucket (created in first step) &mdash; see method-description ([below](Deployment-Upgrade_Parallel.md#bucket-to-bucket-data-copy))
1. Deploy new EC2 stack &mdash; See _Wholly-New Jenkins Service_'s [Instance Provisioning](Deployment-Fresh.md#instance-provisioning) section.

## Bucket-to-Bucket Data-Copy

It will be necessary to execute a third-party copy of data from the source bucket to the destination bucket. This is most-easily done using the AWS CLI &mdash; it is assumed that the CLI has permissions to both the source and destination buckets. (see notes in the dependencies section about "Access to a computer...").

1. Determine the name of the source bucket and path to most-recent backup file
2. Determine the name of the destination bucket
3. Execute a bucket to bucket copy operation similar to: 
    ~~~~
    aws s3 cp s3://<SOURCE_BUCKET>/Backups/sync/<LATEST_SYNC_FILE>.tar s3://<DESTINATION_BUCKET>/Backups/sync/
    ~~~~

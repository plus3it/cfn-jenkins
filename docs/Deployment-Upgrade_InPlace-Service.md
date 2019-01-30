# Upgrading The Jenkins Service - Master Node

## Purpose

This document is intended to walk the automation-user through the process of upgrading a Jenkins service from a previous, template-based deployment.

## Caveats

While this procedure has proven to be fairly reliable, it _does_ depend on success of the to-be-upgraded instance's backups. It is best to verify that the available sync-backups are good before initiating this procedure. Overall, this process should be vetted and run under any relevant site-local policies regarding upgrades.

## Dependencies

In order to use these templates, the following things will be necessary:

* Access to an AWS account
* An IAM user or role with (at least) enough permissions to:
    * Modify classic Elastic LoadBalancers (ELBs)
    * Create/Modify CloudWatch Logs log-groups (Optional: only required if deploying to a region that supports the use of the CloudWatch Logs service and wishing to take advantage of same)
    * Create/Modify DNS records in Route53 (Optional: only required if deploying to a region that supports Route53 and use of Route53 DNS service is desired)
* Access to a computer that has a modern git client
* Access to a computer that has Python installed (and is pip-enabled)
* Access to a computer that has a modern web browser
* Access to a computer that has the AWS CLI installed or _installable_ (e.g., `pip install awscli`)
* Ability to configure the AWS CLI to use the previously-described IAM user or role

## Assumptions/Dependencies

* The automation user has a verified backup of the running configuration (or the ability to do so immediately prior to attempting th
e upgrade)
* The Jenkins-hosting EC2 instance was deployed using the `make_jenkins_EC2-Master-instance.tmplt.json` template
* The "JenkinsRpmName" parameter-value was specified as a version-pinned value in the previous deployment
* Automation user has sufficient privileges to execute an instance-replacing CloudFormation stack-update action

## Workflow Description

While this task can, notionally, be done through both the CloudFormation web UI and the AWS CLI, only procedures for use with the we
b UI have been validated.

### Update Via Web UI

1. Login to the CloudFormation web console
1. Locate the previously-deployed stack
1. Select the  previously-deployed stack
1. Select the "Update Stack" option from the `Action` button/menu
1. Select "Use current template" from the "Select Template" page.
1. Change the value of the "JenkinsRpmName" parameter to the Jenkins RPM you wish to upgrade to.
1. Force a redeployment by:
   * Changing the value of the "AmiId" parameter to the AMI you wish to update to; and/or
   * Changing the value of the "SubnetId" to one of the other subnets in the VPC (if VPC has public and private subnets, change to another subnet of the same type).
1. Note: Other parameters may also be modified. However, doing so is out of scope for this document.
1. Click on the "Next" button to get to the "Options" page
1. Click on the "Next" button to get to the "Review" page
1. Validate all values are as desired. Note that the instance should be shown for replacement in the "Preview your changes" section.
1. Click on the "Update" button to commence the process.
1. Track the update process
    * If the update succeeds, the previous instance will be terminated
    * If the update fails, the new instance will be terminated. If &mdash; per the caveats section &mdash; the previous instance wa
stopped, it will need to be restarted for services to resume.
1. Login to the Jenkins service and verify that previous functionality is present on the replacement node.

### Update Via AWS CLI

**TBD**

## "Under the Covers"

When the stack-update executes, a new EC2 instance is launched in parallel. The application installation/configuration scripts will attempt to find a recent sync-backup in the associated S3 bucket's `Backups/sync/` folder. If it finds a good backup, it will attempt to auto-recover from that data as part of the deployment processes. Once complete, the original instance will be terminated and a new, upgraded instance should be online with all of the data of the prior instance's last sync-backup.

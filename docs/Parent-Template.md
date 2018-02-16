### Parent Template

The function of this template is to provide an "Easy" button for deploying a Jenkins Master node. The provided template invokes the Security Group, S3, IAM, ELB and (master) AutoScaling template. Upon completion of this template's running, an ELB-fronted Jenkins master will be up and ready for initial configuration.

A wide variety of "parent" templates may be needed to deal with the particulars of a given deployment (e.g., environments where permissions for creating IAM roles are separate from those for creating EC2s). This parent is meant as an example suitable for environments where the user has full provisioning-rights within an AWS account.

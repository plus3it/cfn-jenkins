### S3 Bucket

The Jenkins service EC2s typically need access to two buckets: a bucket that hosts the installation-automation scripts and a bucket for backups. The [make_jenkins_S3-bucket.tmplt.json](/Templates/make_jenkins_S3-bucket.tmplt.json) template _only_ takes care of setting up the bucket for backup-activities. The outputs from this template are used by the IAM Role template to create the requisite S3 bucket access-rules in the resultant IAM policy document.

It is assumed that the bucket hosting DOTC-related software, scripts and other "miscellaneous" data will exist outside of the DOTC-Jenkins deployment-silo.

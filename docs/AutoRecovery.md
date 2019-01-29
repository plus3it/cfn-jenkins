## About Auto-Recovery

By default, Jenkins Master nodes deployed by this automation will back themselves up to a designated S3 bucket. This backup-logic contain's two parts:

* Daily: This is a basic, once-per-day, full backup of the Jenkins installation's `${JENKINS_HOME}` directory. This backup is meant as an "ultimate fallback" option in case the running service gets blown up. These backups are (currently) kept for a non-configurable seven-day window. Recovery from these backup images require a manual procedure. As of this document's writing, this procedure is yet to be defined.
* Auto-recovery: This is a basic, high-frequency backup of the Jenkins installation's `${JENKINS_HOME}` directory. This backup is meant to provide a low-RPO recovery-checkpoint. These backup images are desinged to be used by the installation script to "seed" a new Jenkins Master server's installation with data created from a prior instance (or instances). These backups are taken at a non-configurable 20-minute interval an kept for a non-configurable sixty-minute window.

### The Mechanics

The `jenkins_appinstall.sh` includes a logic-block that attempts to locate automatically-recoverable files (those created per the "Auto-recovery" backup-description, above). The script:

1. Attempts to find an auto-recovery TAR file in the `<BACKUP_BUCKET>/<BACKUP_FOLDER>/sync` directory:
    * The `BACKUP_BUCKET` value is retrieved from the `/etc/cfn/Jenkins.envs` file &mdash; previously populated via `cfn-init` from the launched CFn stack's equivalent parameter-value.
    * The `BACKUP_FOLDER` value is retrieved from the `/etc/cfn/Jenkins.envs` file &mdash; previously populated via `cfn-init` from the launched CFn stack's equivalent parameter-value.
    * Candidate recovery files are named `JENKINS_HOME-<MM>.tar` &mdash; where the value of `MM` is the minute at which the job that created the auto-recovery file was initiated. Typically, this value will be one of `00`, `20` or `40`.
1. If a candidate auto-recovery TAR file is found, an attempt will be made to verify that the TAR file is not corrupted
1. If the TAR file passes the verification test, it will be directly extracted from its S3-hosted location and recovered to `${JENKINS_HOME}`
1. The auto-recovery routines will then do a basic check for the suitability of the recovered data.
1. If the recovered data appears to be valid, the install scripts will continue on with installation of Jenkins binaries.

Upon completion of auto-recovery and binary installation, the Jenkins service should restart to the same state that the system that generated the auto-recovery TAR file was in at the time of the file's creation.

### Notes

Raw recovery-times &mdash; and, by extension, total instantiation-time &mdash; is heavily impacted by network throughput between S3 and the Jenkins EC2 instance as well as the performance of the EBS volume hosting the `${JENKINS_HOME}` directory. Recent testing has shown that recovery of a 15GiB `${JENKINS_HOME}` directory typically takes 2-3 minutes using a t3.large with a gp2 EBS volume acting as the recovered to `${JENKINS_HOME}` directory:
* Instance types with slower networking (t2 family) and/or slower EBS performance may take _considerably_ longer
* similarly, S3 service-disruptions will adversely impact recovery-speeds

Overall recovery-time is about double the raw recovery-time. This is due to the validity-test performed against the candidate auto-recovery TAR file. The validity-test takes nearly as long to perform as the actual recovery does.

As of this writing, the auto-recovery mechanisms only attempts recovery from the most-recently created auto-recovery file. Future iterations may be updated to attempt to recover older files in the event the newest file is corrupt.

S3 service-problems may cause the the automated generation of recovery-files to wedge. Currently, there is no anti-wedge logic built into the automated backups. If the S3 service-problem is severe enough for long enough, a number of wedged backup processes may result. These may, ultimately, cause the Jenkins master to become unusably slow:
* For Jenkins Master on standalone EC2, rebooting the instance should clear the problem
* For Jenkins Master on ASG-managed EC2, ASG may interpret this as a failure condition and attempt to correct by replacing the "faulted" EC2 instance.

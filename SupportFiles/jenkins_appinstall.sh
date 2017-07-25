#!/bin/bash
# shellcheck disable=
#
# Script to install and configure Jenkins and related components
#
#################################################################
PROGNAME="$(basename ${0})"
SELMODE="$(awk -F= '/^SELINUX=/{print $2}' /etc/selinux/config)"
JENKDATADIR="${JENKINS_HOME_PATH:-UNDEF}"
JENKBKUPBKT="${JENKINS_BACKUP_BUCKET:-UNDEF}"
JENKBKUPFLD="${JENKINS_BACKUP_FOLDER:-UNDEF}"
JENKHOMEURL="s3://${JENKBKUPBKT}/${JENKBKUPFLD}/backups/sync/JENKINS_HOME/"
CFNENDPT="${JENKINS_CFN_ENDPOINT:-UNDEF}"
JENKINITTOK="/var/lib/jenkins/secrets/initialAdminPassword"

##
## Set up an error logging and exit-state
function err_exit {
   local ERRSTR="${1}"
   local SCRIPTEXIT=${2:-1}

   # Our output channels
   echo "${ERRSTR}" > /dev/stderr
   logger -t "${PROGNAME}" -p kern.crit "${ERRSTR}"

   # Need our exit to be an integer
   if [[ ${SCRIPTEXIT} =~ ^[0-9]+$ ]]
   then
      exit "${SCRIPTEXIT}"
   else
      exit 1
   fi
}

# Install Jenkins from RPM/yum
yum install -y jenkins || err_exit 'Jenkins install failed'
     
# Restore JENKINS_HOME content (if available)
echo "Attempting to restore JENKINS_HOME from S3... "
su -s /bin/bash jenkins -c \"/usr/bin/aws s3 sync --quiet ${JENKHOMEURL} /var/lib/jenkins"

if [[ -f /var/lib/jenkins/config.xml ]]
then
   echo "Restored JENKINS_HOME from S3."
   FRESHINSTALL=1
else
   echo "No restorable S3 content found. Assuming this is a fresh Install."
   FRESHINSTALL=0
fi
     
# Start Jenkins
systemctl enable jenkins
systemctl start jenkins
     
# Install Jenkins backup cron-job
(
 crontab -l 2> /dev/null ;
 echo "# Daily backup of JENKINS_HOME to S3" ;
 printf "0 23 * * * su -s /bin/bash jenkins -c " ;
 printf "\"/usr/bin/aws s3 sync --delete --quiet /var/lib/jenkins ${JENKHOMEURL}" ;
 echo "/daily/\$(date '+%A')\"" ;
 echo "# Sync JENKINS_HOME to S3 for re-deployments" ;
 printf "*/20 * * * * su -s /bin/bash jenkins -c " ;
 printf "\"/usr/bin/aws s3 sync --delete --quiet /var/lib/jenkins ${JENKHOMEURL}" ;
 echo "/sync/JENKINS_HOME/\"";
) | crontab -

# backup JENKINS_HOME to S3
*/20 * * * * /usr/bin/aws s3 sync --delete --quiet /var/lib/jenkins s3://devops-toolchain/jenkins-master/backups/sync/JENKINS_HOME/

     
# Re-enable SELinux
print "Reverting SELinux enforcing-mode... "
setenforce "${SELMODE}" || err_exit 'Could not change SEL-mode'

# Display initial admin token on fresh install first-boot
if [[ ${FRESHINSTALL} -eq 0 ]]
then
   while [[ ! -f ${JENKINITTOK} ]]
   do
      echo "Sleeping for 15s to allow secrets-file to populate... "
      sleep 15
   done

   echo "########################################"
   echo "##"
   echo "## Jenkins unlock-string is:"
   printf "##   * "
   echo "$(cat ${JENKINITTOK})"
   echo "##"
   echo "########################################"
fi

#!/bin/bash
# shellcheck disable=SC2005,SC2059
#
# Script to install and configure Jenkins and related components
#
#################################################################
# shellcheck disable=SC2086
PROGNAME="$(basename ${0})"
while read -r JNKENV
# Read args from envs file
do
   # shellcheck disable=SC2163
   export "${JNKENV}"
done < /etc/cfn/Jenkins.envs
SELMODE="$(awk -F= '/^SELINUX=/{print $2}' /etc/selinux/config)"
JENKDATADIR="${JENKINS_HOME_PATH:-UNDEF}"
JENKBKUPBKT="${JENKINS_BACKUP_BUCKET:-UNDEF}"
JENKBKUPFLD="${JENKINS_BACKUP_FOLDER:-UNDEF}"
JENKHOMEURL="s3://${JENKBKUPBKT}/${JENKBKUPFLD}"
JENKINITTOK="${JENKDATADIR}/secrets/initialAdminPassword"

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
if [[ $(aws s3 ls ${JENKHOMEURL} > /dev/null 2>&1 )$? -gt 0 ]]
then
   printf 'Cannot find a recovery-directory in %s\n' "${JENKHOMEURL}"
else
   echo "Attempting to restore JENKINS_HOME from S3... "
   sudo -H -u jenkins /usr/bin/aws s3 sync --quiet "${JENKHOMEURL}/sync/JENKINS_HOME/" "${JENKDATADIR}" || true
fi

if [[ -f ${JENKDATADIR}/config.xml ]]
then
   echo "Restored JENKINS_HOME from S3."
   FRESHINSTALL=1
else
   echo "No restorable S3 content found. Assuming this is a fresh Install."
   FRESHINSTALL=0
fi
     
# Start Jenkins
echo "Starting and enabling Jenkins service... "
systemctl enable jenkins
systemctl start jenkins

# Re-enable SELinux
printf "Reverting SELinux enforcing-mode... "
# shellcheck disable=SC2015
setenforce "${SELMODE}" && echo || err_exit 'Could not change SEL-mode'

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

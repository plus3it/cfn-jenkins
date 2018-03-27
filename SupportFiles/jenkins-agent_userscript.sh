#!/bin/bash
# shellcheck disable=SC2163,SC2015
#
# Script to install and configure Jenkins and related components
# onto Jenkins Linux agent-host
#
#################################################################
# shellcheck disable=SC2086
PROGNAME="$(basename ${0})"
# Pull settings from env-file
while read -r JENKENV
do
   export "${JENKENV}"
done < /etc/cfn/JenkinsAgent.envs
WORKDIR=${JENKINS_WORKDIR:-UNDEF}
HOMEDIR=${HOME:-UNDEF}
JENKMSTR=${JENKINS_MASTER:-UNDEF}
ZAPARCHIVE="/tmp/ZAP_Linux.tar.gz"

# Misc error-handler
function err_exit {
   local ERRSTR="${1}"
   local SCRIPTEXIT=${2:-1}

   # Our output channels
   # echo "${ERRSTR}" > /dev/stderr
   logger -t "${PROGNAME}" -p kern.crit "${ERRSTR}"

   # Need our exit to be an integer
   if [[ ${SCRIPTEXIT} =~ ^[0-9]+$ ]]
   then
      exit "${SCRIPTEXIT}"
   else
      exit 1
   fi
}

# Make sure we have the var-data we need...
if [[ ${WORKDIR} = UNDEF ]] ||
   [[ ${HOMEDIR} = UNDEF ]] ||
   [[ ${JENKMSTR} = UNDEF ]]
then
   err_exit 'Missing data to drive rest of script'
fi

# Install the JNLP
printf 'Re-rooting to %s... ' "${WORKDIR}"
cd ${WORKDIR} && echo '' || \
  err_exit "Failed to chdir to ${WORKDIR}"
printf 'Pulling agent.jar from %s' "${JENKMSTR}"
curl -OskL https://"${JENKMSTR}"/jnlpJars/agent.jar && echo || \
  err_exit "Failed to pull agent.jar"

# Install the OWASP-ZAP agent-software
if [[ -f ${ZAPARCHIVE} ]]
then
   printf 'Re-rooting to %s... ' "${HOMEDIR}"
   cd ${HOMEDIR} && echo '' || \
     err_exit "Failed to chdir to ${HOMEDIR}"

   printf 'Creating a dearchiving work-directory'
   install -d -m 0700 wrk && echo '' || \
     err_exit 'Failed to create dearchiving work-directory'

   printf 'Descending into work-directory'
   cd wrk && echo '' || \
     err_exit "Failed to descend into work-directory"

   printf 'De-archiving ZAP_Linux.tar.gz... '
   tar zxf "${ZAPARCHIVE}" && echo 'Success' || \
     err_exit 'De-archive operation failed'

   printf 'Re-rooting de-archived files'
   mv ZAP* ../OWASP-ZAP && echo || \
     err_exit 'Failed to re-root de-archived files'

   # Cleanup (being nice - don't really care if fails)
   cd .. && rm -rf wrk
else
   err_exit 'OWASP ZAP agent-archive not found'
fi

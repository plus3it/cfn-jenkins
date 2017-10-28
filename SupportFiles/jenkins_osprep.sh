#!/bin/bash
#
# Install supplemental RPMs
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
EPELREPO="${JENKINS_EPEL_REPO:-UNDEF}"
FWSVCS=(
      http
      https
      jenkins
   )
JNKPORTS=(
         8080
         32000
         45158
        )
BONUSRPMS=(
           git
           bzip2
           pylint
           pyunit
           junit
            )

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


##
## Add firewall exceptions
echo "Adding exceptions to firewalld... "
setenforce 0 || err_exit "Failed to temp-disable SELinux"
firewall-offline-cmd --enabled

## Create Jenkins firewalld rule-set
printf "Adding new firewalld service-def... "
firewall-cmd --permanent --new-service=jenkins || \
  err_exit 'Failed creating firewalld service-name'
printf "Adding short-description to service-def... "
firewall-cmd --permanent --service=jenkins --set-short="Jenkins Service Ports" \
  || err_exit 'Failed adding short-description'
printf "Adding long-description to service-def... "
firewall-cmd --permanent --service=jenkins \
  --set-description="Jenkins service firewalld port exceptions" || \
    err_exit 'Failed adding long-description'

for PORT in "${JNKPORTS[@]}"
do
   printf "Adding firewalld exception for %s/tcp... " "${PORT}"
   firewall-cmd --service=jenkins --add-port="${PORT}"/tcp --permanent || \
     err_exit "Failed to add firewalld exception for ${PORT}/tcp."
done

## Activate firewalld service-definitions
for SVC in "${FWSVCS[@]}"
do
   printf "Activating %s firewalld service..." "${SVC}"
   firewall-cmd --permanent --add-service="${SVC}" || \
     err_exit "Failed to activate ${SVC} firewalld service"
done

printf "Reloading firewalld configuration... "
firewall-cmd --reload || err_exit "Failed reloading firewalld configuration."
setenforce 1 || err_exit "Failed to re-enable SELinux"

# Install extra RPMs
echo "Installing supplemental RPMs... "
yum --enablerepo="${EPELREPO}" install -y "${BONUSRPMS[@]}" || \
  err_exit "Failed to install one or more requested RPMs."

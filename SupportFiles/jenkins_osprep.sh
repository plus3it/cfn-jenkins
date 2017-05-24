#!/bin/bash
#
# Install supplemental RPMs
#
#################################################################
PROGNAME="$(basename ${0})"
EPELREPO="${JENKINS_EPEL_REPO:-UNDEF}"
FWPORTS=(
         80
         443
         8080
         32000
        )
BONUSRPMS=(
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
setenforce 0 || err_exit "Failed to temp-disable SELinux"
firewall-offline-cmd --enabled
for PORT in "${FWPORTS}"
do
   printf "Adding firewalld exception for ${PORT}/tcp... "
   firewall-cmd --zone=public --add-port=${PORT}/tcp --permanent || \
     err_exit "Failed to add firewalld exception for ${PORT}/tcp."
done
printf "Reloading firewalld configuration... "
firewall-cmd --reload || err_exit "Failed reloading firewalld configuration."
setenforce 1 || err_exit "Failed to re-enable SELinux"

# Install extra RPMs
yum --enablerepo="${EPELREPO" install -y "${BONUSRPMS}" || \
  err_exit "Failed to install one or more requested RPMs."

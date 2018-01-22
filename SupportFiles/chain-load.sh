#!/bin/bash
#
# Script to download and install Agent-extensions from other sources
#
#################################################################
# shellcheck disable=SC2086
PROGNAME="$(basename ${0})"
SCRIPTHOME="${HOME:-/root}"

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

# Make git run quietly...
quiet_git() {
   if [[ $( git "$@" < /dev/null > /dev/null 2>&1 )$? -eq 0 ]]
   then
      echo "Git-fetch successful"
   else
      err_exit "Git-fetch failed"
   fi
}

# Create git staging-area as needed
if [[ -d ${SCRIPTHOME}/git ]]
then
   echo "Git stagining-area already exists"
else
   printf "Creating central location for Git-hosted resources... "
   # shellcheck disable=SC2015
   install -d -m 000700 ${SCRIPTHOME}/git && echo "Success" || \
     err_exit "Failed creating git staging-area."
fi


#########################################
##                                     ##
## INSERT OPTIONAL LOGIC TO CHAIN-LOAD ##
##               (BELOW)               ##
#########################################


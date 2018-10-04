The dotc-jenkins project's Agent templates deploy fairly generic agents. To facilitate extention of agent-nodes' functionality, a "chain-load" callout is provided in the relevant CloudFormation templates. These callouts allow the template user to provide a network-fetchable script-location. This fetched script should be designed to manage the further invocation of other scripts (see below example).

~~~~
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
   install -d -m 000700 ${SCRIPTHOME}/git && echo "Success" || \
     err_exit "Failed creating git staging-area."
fi


#########################################
##                                     ##
## INSERT OPTIONAL LOGIC TO CHAIN-LOAD ##
##               (BELOW)               ##
#########################################

quiet_git clone <REPO_URL>/<PROJECT_NAME>.git ${SCRIPTHOME}/git/<PROJECT_NAME>
bash -xe ${SCRIPTHOME}/git/<PROJECT_NAME>/<INVOKED_SCRIPT>

curl -skL <SCRIPT_URL> | bash -xe -

aws s3 copy s3://<BUCKET_NAME>/<FILE_NAME> ${SCRIPTHOME}/<FILE_NAME>
bash -xe ${SCRIPTHOME}/<FILE_NAME>

~~~~

Note 1: It is recommended that content added by the template-user to the chain-script include error/exit-logic. This will better ensure that agents are actually configured the way the template-user thinks they're configured.
Note 2: The chain script in this repository is for *example purposes only*. The template-users' real chain-scripts should be kept in a protected location that allows `curl`-based fetching with passed user-credentials or API tokens. Particular care in protecting the "real" chain-script if that chain-script contains sensitive data.

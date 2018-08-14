#!/bin/bash
#
# Script to download and install Agent-extensions from other sources
#
#################################################################
# shellcheck disable=SC2086
PROGNAME="$(basename ${0})"
SCRIPTHOME="${HOME:-/root}"
OPENSHIFTS3URI=""
OPENSHIFTCLIENTBNDL="openshift-origin-client-tools-v3.6.1-008f2d5-linux-64bit.tar.gz"
OPENSHIFTCLIENTDIR="openshift-origin-client-tools-v3.6.1-008f2d5-linux-64bit"
S2IBNDL="source-to-image-v1.1.7-226afa1-linux-amd64.tar.gz"
S2IDIR="source-to-image-v1.1.7-226afa1-linux-amd64"
TESTRPMS=(
      python
      python2-pip
      python3
   )
TESTVENV=(
      pip2
      pip3.4
   )

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

# check for Python and pip RPM installations
for RPM in "${TESTRPMS[@]}"
do
if [[ $(rpm --quiet -qa "${RPM}" )$? -eq 0 ]]
then
    echo "${RPM} is already installed"
else
    printf "Installing %s..." "${RPM}"
    yum install -y "${RPM}" && echo "Success" || err_exit "Failed installing ${RPM}"
fi
done

# check for pip3 installation
if pip3 --version >/dev/null 2>&1; then
        echo pip3 is installed
else
    printf "Installing Pip3"
    curl https://bootstrap.pypa.io/get-pip.py | python3.4  && echo "Success" # no rpm in epel
    pip3.4 install -U pip && echo "Success" || err_exit "Failed to install pip3"
fi

# check for virtualenv installation
for VENV in "${TESTVENV[@]}"
do
if [[ $("${VENV}" list | grep virtualenv )$? -eq 0 ]]
then
    echo "${VENV} is already installed"
else
    printf "Installing %s..." "${VENV}"
    ${VENV} install -U virtualenv && echo "Success" || err_exit "Failed installing ${VENV} virtualenv"
fi
done

#Set SELinux to permissive
printf "Setting SELinux to Permissive to support use of yum inside docker containers..."
setenforce 0 && echo "Success" || err_exit "Failed to set SELinux to permissive"

#Install OpenShift Origin CLI
printf "Downloading OpenShift Origin Client..."
wget ${OPENSHIFTS3URI}/${OPENSHIFTCLIENTBNDL} && echo "Success" || err_exit "Failed to download OC Client Binary"
printf "Extracting OpenShift Origin Client..."
tar -xzf ${OPENSHIFTCLIENTBNDL} && echo "Success" || err_exit "Failed to extract oc client"
printf "Installing OpenShift Origin Client..."
cp ${OPENSHIFTCLIENTDIR}/oc /bin/ && echo "Success" || err_exit "Failed to copy oc client"
printf "Changing Permissions for OpenShift Origin Client..."
chmod 555 /bin/oc && echo "Success" || err_exit "Failed to set permission on oc client"

#Install s2i binary
printf "Downloading s2i bundle..."
wget ${OPENSHIFTS3URI}/${S2IBNDL} && echo "Success" || err_exit "Failed to download S2I Bundle"
printf "Extracting S2I bundle..."
tar -xzf ${S2IBNDL} && echo "Success" || err_exit "Failed to extract S2I Bundle"
printf "Installing S2I binary..."
cp s2i /bin/ && echo "Success" || err_exit
chmod 555 /bin/s2i && echo "Success" || err_exit "Failed to install S2I Binary"

#Install docker
printf "Installing docker..."
yum install -y docker && echo "Success" || err_exit "Failed to install docker"

#Create OS group for docker access
printf "Creating docker group..."
groupadd docker && echo "Success" || err_exit "Failed to add docker group"

#Add jenkins to the docker group
printf "Adding jenkins to docker group..."
usermod -aG docker jenkins && echo "Success" || err_exit "Failed to add jenkins user to docker group"

#Create a directory to store the docker data
printf "Creating docker storage directory..."
install -d -m 000711 -o root -g root /var/jenkins/docker && echo "Success" || err_exit "Failed to create docker storage directory"

#Configure docker to use the storage directory
printf "Configuring docker to use storage directory..."
cat << EOF | tee /etc/docker/daemon.json
{
        "graph": "/var/jenkins/docker"
}
EOF
echo "Success" || err_exit "Failed to configure docker for storage directory"

#Configure the docker deamon to use artifactory as a repository
printf "Configuring docker daemon..."
cat << EOF >> /etc/sysconfig/docker
# /etc/sysconfig/docker

# Modify these options if you want to change the way the docker daemon runs
OPTIONS='--selinux-enabled --log-driver=journald --signature-verification=false --add-registry=docker-code.artifactory.dicelab.net'
if [ -z "${DOCKER_CERT_PATH}" ]; then
        DOCKER_CERT_PATH=/etc/docker
fi
EOF
echo "Success" || err_exit "Failed to configure docker daemon"

#Enable the docker service
printf "Enabling docker service..."
systemctl enable docker.service && echo "Success" || err_exit "Failed to enable docker service"

#Add OVL support for running yum in docker
printf "Installing yum-plugin-ovl..."
yum install -y yum-plugin-ovl && echo "Success" || err_exit "Failed to install yum-plugin-ovl"

#Starting docker service
printf "Starting docker service..."
systemctl start docker && echo "Success" || err_exit "Failed to start docker service"

#Install twistcli
printf "Downloading twistcli..."
wget https://cdn.twistlock.com/releases/p35o2jfb/twistlock_2_3_98.tar.gz && echo "Success" || err_exit "Failed to download twistcli"
printf "Unpacking twistcli..."
tar -xzf twistlock_2_3_98.tar.gz && echo "Success" || err_exit "Failed to unpack twistcli"
printf "Installing twistcli..."
cp linux/twistcli /bin/ && echo "Success" || err_exit "Failed to install twistcli"

# restart the system to capture group changes for docker
systemctl reboot

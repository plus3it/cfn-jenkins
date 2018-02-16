### Security Groups

The [make_jenkins_SGs.tmplt.json](/Templates/make_jenkins_SGs.tmplt.json) file sets up the security group used to gate network-access to the Jenkins elements. The DOTC-Jenkins design assumes that the entirety of the Jenkins-deployment exists within a security-silo. This silo contains only the Jenkins-service elements. The security-group created by this template is designed to foster communication between service-elements while allowing network-ingress and -egress to the silo _only_ through the Internet-facing load-balancer.

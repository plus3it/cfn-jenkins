### "Classic" Elastic LoadBalancer

All of the Jenkins EC2 instances launched by this project should be deployed into a VPC's private subnets. The Elastic LoadBalancer &mdash; created by the [make_jenkins_ELBv1-pub-autoscale.tmplt.json](/Templates/make_jenkins_ELBv1-pub-autoscale.tmplt.json) template &mdash; provides the public-facing ingress-/egress-point to the Jenkins service-deployment. This ELB provides the bare-minimum transit services required for the Jenkins Master node to be usable from client requests arriving via the public Internet.

A "Classic" ELB is the currently-preferred method for setting up Internet-facing access to the Jenkins Master node. Use of an Application LoadBalance may become preferred in later iterations. However, the comparative simplicity of configureing the "Classic" ELB makes it the currently-preferred option.

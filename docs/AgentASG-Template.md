### Auto-Scaling Agent Instance(s)

The [make_jenkins_Agent-autoscale.tmplt.json](/Templates/make_jenkins_Agent-autoscale.tmplt.json) template &mdash; along with deployment-automation helper-scripts &mdash; creates an EC2 Launch Configuration tied to an AutoScaling Group. This configuration is intended primarily to improve the availability of the individual Jenkins agent node. The AutoScaling group keeps the number of active nodes at "1": in the event of a failure detected in the currently-active node, the AutoScaling group will launch a replacement node. When the replacement node reaches an acceptable state, the original node is terminated.

It is expected that at least one auto-scaling agent will be deployed per private subnet in the VPC. This will help distribute the load associated with client-initiated Jenkins jobs and allow individual agents to be updated without rendering the overall Jenkins solution unable to run jobs.

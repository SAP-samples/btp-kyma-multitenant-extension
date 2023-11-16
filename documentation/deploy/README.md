# Deploy

In this section you will learn how to deploy all components in your Kyma cluster.

## Prerequisites

- Have all the tools listed in the section [Set Up the Development Environment](../../documentation/prepare/set-up-local-environment/README.md) installed
- Have all properties necessary for the JDBC connection prepared. See section [Get and Configure your SAP HANA Cloud Instance](../../documentation/prepare/configure-hana/README.md)
- Have the username and password for email account you are going to use

## Deployment Options

 In the following sections, we will explain two options to deploy the components. There is a [complete manual way](../../documentation/deploy/manual-deployment/README.md) of deploying the components or you can use the [setup script](../../documentation/appendix/script-deployment/README.md) which is provided in the [setup](../../code/setup).  Essentially the script does the same as explained in the manual deployment sections so we recommend to go through the manual deployment once to understand what is happening within the script. If you plan to use the script after the manual deployment, we recommend to create a copy of the [k8s](../../code/easyfranchise/deployment/k8s) folder as you need todo some changes to the yaml files for the manual deployment, which need to be reverted for the script. 

## Section Content
1. [Prepare the Deployment](../../documentation/deploy/prepare-deployment/README.md)
1. [Deploy the Artifacts Manually](../../documentation/deploy/manual-deployment/README.md)
1. [Build Script to Automate Deployment](../../documentation/appendix/script-deployment/README.md)

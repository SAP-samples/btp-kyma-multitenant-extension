# Table of Content 


- [EasyFranchise](README.md#easyfranchise)

code/approuter:

- [Approuter](code/approuter/readme.md#approuter)
    - [Deplyoment to Kyma cluster.](code/approuter/readme.md#deplyoment-to-kyma-cluster)
    - [Check the user and tenant info](code/approuter/readme.md#check-the-user-and-tenant-info)
  - [Reference](code/approuter/readme.md#reference)


code/broker: 

- [easyfranchise-broker](code/broker/readme.md#easyfranchise-broker)
    - [Multi-Tenancy Implementation](code/broker/readme.md#multi-tenancy-implementation)
      - [Implement Callback for on-boarding / off-boarding](code/broker/readme.md#implement-callback-for-on-boarding--off-boarding)
      - [Add scope for multi-tenancy callback in XSUAA instance](code/broker/readme.md#add-scope-for-multi-tenancy-callback-in-xsuaa-instance)
      - [Add SaaS-Registry](code/broker/readme.md#add-saas-registry)
      - [Create APIRule for subsction URL](code/broker/readme.md#create-apirule-for-subsction-url)
      - [Approuter Configuration](code/broker/readme.md#approuter-configuration)
      - [Reference](code/broker/readme.md#reference)

code/email-service:

- [Email Service](code/email-service/README.md#email-service)

code/business-partner-mock-server

- [Business Partner Mock Service](code/business-partner-mock-server/readme.md#business-partner-mock-service)

documentation

- [Documentation Structure](documentation/README.md#documentation-structure)
- [Known Issues](documentation/README.md#known-issues)
- [How to obtain Support](documentation/README.md#how-to-obtain-support)

documentation/appendix


- [Appendix](documentation/appendix/readme.md#appendix)
  - [Run AppRouter locally](documentation/appendix/readme.md#run-approuter-locally)
  - [Run Business Partner mock service](documentation/appendix/readme.md#run-business-partner-mock-server)

documentation/appendix/approuter-local

- [How to setup AppRouter for local development](documentation/appendix/approuter-local/readme.md#how-to-setup-approuter-for-local-development)

documentation/appendix/business-partner-mock

- [How to Use the Business Partner Mock Server](documentation/appendix/business-partner-mock/readme.md)
  - [Run locally](documentation/appendix/business-partner-mock/readme.md#run-locally)
    - [Test locally](documentation/appendix/business-partner-mock/readme.md#test-locally)
  - [Deploy to Kyma cluster](documentation/appendix/business-partner-mock/readme.md#deploy-to-kyma-cluster)

documentation/exploration

- [Exploration of application components](documentation/exploration/README.md#exploration-of-application-components)
  - [Additional Information about the Java Microservices](documentation/exploration/README.md#additional-information-about-the-java-microservices)

documentation/exploration/approuter

- [AppRouter](documentation/exploration/approuter#approuter)
    - [Configuring AppRouter](documentation/exploration/approuter/README.md#configuring-approuter)
    - [Extending the AppRouter with a custom middleware.](documentation/exploration/approuter/README.md#extending-the-approuter-with-a-custom-middleware)
  - [Authorization and Trust Management Service (aka. XSUAA)](documentation/exploration/approuter#authorization-and-trust-management-service-aka-xsuaa)
  - [(Optional) Authentication using Identity Authentication Service (IAS)](documentation/exploration/approuter#optional-authentication-using-identity-authentication-service-ias)

documentation/exploration/broker

- [Broker / SaaS Provisioning Service](documentation/exploration/broker/README.md#broker--saas-provisioning-service)
  - [Implement callback functions](documentation/exploration/broker/README.md#implement-callback-functions)
  - [Deploy broker with service](documentation/exploration/broker/README.md#deploy-broker-with-service)
  - [Create SaaS Provision service instance](documentation/exploration/broker/README.md#create-saas-provision-service-instance)

documentation/exploration/db-service


- [Database Service](documentation/exploration/db-service/README.md#database-service)
- [Service Implementation](documentation/exploration/db-service/README.md#service-implementation)
- [Persistence Implementation](documentation/exploration/db-service/README.md#persistence-implementation)
  - [Overview](documentation/exploration/db-service/README.md#overview)
  - [Entities](documentation/exploration/db-service/README.md#entities)
  - [Multitenancy](documentation/exploration/db-service/README.md#multitenancy)
      - [Onboarding](documentation/exploration/db-service/README.md#onboarding)
      - [Schemas](documentation/exploration/db-service/README.md#schemas)
      - [Tenant Aware Operations](documentation/exploration/db-service/README.md#tenant-aware-operations)
- [List of API Endpoints](documentation/exploration/db-service/README.md#list-of-api-endpoints)

documentation/exploration/ef-service

- [Easy Franchise Service](documentation/exploration/ef-service/README.md#easy-franchise-service)
  - [Service Implementation](documentation/exploration/ef-service/README.md#service-implementation)
  - [Rest Client](documentation/exploration/ef-service/README.md#rest-client)
  - [Scheduler](documentation/exploration/ef-service/README.md#scheduler)
  - [List Of API Endpoints](documentation/exploration/ef-service/README.md#list-of-api-endpoints)
    - [Tenant aware API Endpoints](documentation/exploration/ef-service/README.md#tenant-aware-api-endpoints)
    - [Tenant in depended API Endpoints](documentation/exploration/ef-service/README.md#tenant-in-depended-api-endpoints)

documentation/exploration/email-service

- [Email Service](documentation/exploration/email-service/README.md#email-service)
  - [Service Implementation](documentation/exploration/email-service/README.md#service-implementation)
  - [List of API Endpoints](documentation/exploration/email-service/README.md#list-of-api-endpoints)

documentation/exploration/bp-service

- [S4 Service](documentation/exploration/bp-service/README.md#bp-service)
  - [Understanding the OData endpoint of SAP S/4HANA](documentation/exploration/bp-service/README.md#understanding-the-odata-endpoint-of-sap-s4hana)
  - [Service Implementation](documentation/exploration/bp-service/README.md#service-implementation)
  - [List of API Endpoints](documentation/exploration/bp-service/README.md#list-of-api-endpoints)

documentation/exploration/ui

- [User Interface](documentation/exploration/ui/README.md#user-interface)
- [Structure of application](documentation/exploration/ui/README.md#structure-of-application)
  - [Main.js](documentation/exploration/ui/README.md#mainjs)
  - [App.vue](documentation/exploration/ui/README.md#appvue)
  - [FranchisesOverview.vue](documentation/exploration/ui/README.md#franchisesoverviewvue)
  - [FranchiseDetails.vue](documentation/exploration/ui/README.md#franchisedetailsvue)
  - [AdminCorner.vue](documentation/exploration/ui/README.md#admincornervue)

documentation/introduction

- [Introduction](documentation/introduction/README.md#introduction)

documentation/introduction/business-story

- [Business story](documentation/introduction/business-story/README.md#business-story)
- [Solution](documentation/introduction/business-story/README.md#solution)
- [Involved personas](documentation/introduction/business-story/README.md#involved-personas)
    - [Partner EasyFranchise](documentation/introduction/business-story/README.md#partner-easyfranchise)
    - [Customer](documentation/introduction/business-story/README.md#customer)

documentation/introduction/links

- [Links](documentation/introduction/links/README.md#links)

documentation/introduction/prerequisites

- [Prerequisites](documentation/introduction/prerequisites/README.md#prerequisites)
  - [Systems & Services](documentation/introduction/prerequisites/README.md#systems--services)
  - [Tools](documentation/introduction/prerequisites/README.md#tools)

documentation/introduction/scenario-focus

- [Scenario Focus](documentation/introduction/scenario-focus/README.md#scenario-focus)
  - [Who Is This Tutorial For?](documentation/introduction/scenario-focus/README.md#who-is-this-tutorial-for)
  - [Focus Topics of the Tutorial](documentation/introduction/scenario-focus/README.md#focus-topics-of-the-tutorial)

documentation/introduction/whats-new

- [What's new](documentation/introduction/whats-new/READMDE.md#whats-new)
  - [2021-09-30](documentation/introduction/whats-new/READMDE.md#2021-09-30)

documentation/step-by-step-guide

- [Step-by-step Guide](documentation/step-by-step-guide/README.md#step-by-step-guide)

documentation/step-by-step-guide/onboarding-offboarding/configure-destination

- [Configure destination](documentation/step-by-step-guide/onboarding-offboarding/configure-destination/README.md#configure-destination)

documentation/step-by-step-guide/onboarding-offboarding/create-consumer-subaccount

- [Create Customer Subaccount](documentation/step-by-step-guide/onboarding-offboarding/create-consumer-subaccount/README.md#create-customer-subaccount)

documentation/step-by-step-guide/onboarding-offboarding/manage-users

- [Manage Users](documentation/step-by-step-guide/onboarding-offboarding/manage-users/README.md#manage-users)

documentation/step-by-step-guide/onboarding-offboarding/offboarden-a-customer

- [Offboarden a customer](documentation/step-by-step-guide/onboarding-offboarding/offboarden-a-customer/README.md#offboarden-a-customer)

documentation/step-by-step-guide/onboarding-offboarding/send-instructions-to-customer

- [Send Instructions to Customer](documentation/step-by-step-guide/onboarding-offboarding/send-instructions-to-customer/README.md#send-instructions-to-customer)

documentation/step-by-step-guide/onboarding-offboarding/subscribe-easyfranchise-app

- [Subscribe EasyFranchise App](documentation/step-by-step-guide/onboarding-offboarding/subscribe-easyfranchise-app/README.md#subscribe-easyfranchise-app)

documentation/step-by-step-guide/onboarding-offboarding/test-multitenancy

- [Test Multitenancy](documentation/step-by-step-guide/onboarding-offboarding/test-multitenancy/README.md#test-multitenancy)

documentation/step-by-step-guide/preparation

- [Preparation](documentation/step-by-step-guide/preparation/README.md#preparation)

documentation/step-by-step-guide/preparation/configure-hana

- [Get and Configure your HANA Cloud Instance](documentation/step-by-step-guide/preparation/configure-hana/README.md#get-and-configure-your-hana-cloud-instance)
  - [Create the SAP HANA Cloud Instance](documentation/step-by-step-guide/preparation/configure-hana/README.md#create-the-sap-hana-cloud-instance)
- [Create EFADMIN](documentation/step-by-step-guide/preparation/configure-hana/README.md#create-efadmin)
- [How to find JDBC Connection Properties](documentation/step-by-step-guide/preparation/configure-hana/README.md#how-to-find-jdbc-connection-properties)

documentation/step-by-step-guide/preparation/configure-s4

- [SAP S/4HANA Cloud or Business Partner Mock Server](documentation/step-by-step-guide/preparation/configure-s4/README.md)
- [SAP S/4HANA Cloud](documentation/step-by-step-guide/preparation/configure-s4/README.md#sap-s4hana-cloud)
- [Business Partner Mock Server](documentation/step-by-step-guide/preparation/configure-s4/README.md#business-partner-mock-server)

documentation/step-by-step-guide/preparation/set-up-local-environment

- [Development Setup](documentation/step-by-step-guide/preparation/set-up-local-environment/README.md#development-setup)
  - [Windows User](documentation/step-by-step-guide/preparation/set-up-local-environment/README.md#windows-user)
  - [Local Development and Testing](documentation/step-by-step-guide/preparation/set-up-local-environment/README.md#local-development-and-testing)
    - [Java - Java Development Kit](documentation/step-by-step-guide/preparation/set-up-local-environment/README.md#java---java-development-kit)
    - [Maven](documentation/step-by-step-guide/preparation/set-up-local-environment/README.md#maven)
    - [Node.js](documentation/step-by-step-guide/preparation/set-up-local-environment/README.md#nodejs)
  - [Deployment to Kyma](documentation/step-by-step-guide/preparation/set-up-local-environment/README.md#deployment-to-kyma)
    - [Kubernetes CLI](documentation/step-by-step-guide/preparation/set-up-local-environment/README.md#kubernetes-cli)
    - [Docker](documentation/step-by-step-guide/preparation/set-up-local-environment/README.md#docker)
    - [Docker Hub Account](documentation/step-by-step-guide/preparation/set-up-local-environment/README.md#docker-hub-account)

documentation/step-by-step-guide/preparation/setup-btp-environment

- [SAP BTP Account](documentation/step-by-step-guide/preparation/setup-btp-environment/README.md#sap-btp-account)
  - [Create the provider subaccount.](documentation/step-by-step-guide/preparation/setup-btp-environment/README.md#create-the-provider-subaccount)
  - [Prepare Environments](documentation/step-by-step-guide/preparation/setup-btp-environment/README.md#prepare-environments)

documentation/step-by-step-guide/run-app-in-kyma

- [Run the Apllication in Kyma](documentation/step-by-step-guide/run-app-in-kyma/README.md#run-the-apllication-in-kyma)
  - [Prerequisites](documentation/step-by-step-guide/run-app-in-kyma/README.md#prerequisites)
  - [Deployment Options](documentation/step-by-step-guide/run-app-in-kyma/README.md#deployment-options)
  - [Chapter content](documentation/step-by-step-guide/run-app-in-kyma/README.md#chapter-content)

documentation/step-by-step-guide/run-app-in-kyma/common-tasks

- [Common Tasks](documentation/step-by-step-guide/run-app-in-kyma/common-tasks/README.md#common-tasks)
  - [Common Tasks](documentation/step-by-step-guide/run-app-in-kyma/common-tasks/README.md#common-tasks-1)
    - [Get Kubeconfig](documentation/step-by-step-guide/run-app-in-kyma/common-tasks/README.md#get-kubeconfig)
    - [Create Namespaces](documentation/step-by-step-guide/run-app-in-kyma/common-tasks/README.md#create-namespaces)
    - [Secrets](documentation/step-by-step-guide/run-app-in-kyma/common-tasks/README.md#secrets)
      - [DB-Config](documentation/step-by-step-guide/run-app-in-kyma/common-tasks/README.md#db-config)
      - [Email-Secret](documentation/step-by-step-guide/run-app-in-kyma/common-tasks/README.md#email-secret)
      - [Registry Secret](documentation/step-by-step-guide/run-app-in-kyma/common-tasks/README.md#registry-secret)
        - [Docker Hub](documentation/step-by-step-guide/run-app-in-kyma/common-tasks/README.md#docker-hub)

documentation/step-by-step-guide/run-app-in-kyma/manual-deployment

- [Deployment](documentation/step-by-step-guide/run-app-in-kyma/manual-deployment/README.md#deployment)
  - [Backend Deployment](documentation/step-by-step-guide/run-app-in-kyma/manual-deployment/README.md#backend-deployment)
    - [Configmap](documentation/step-by-step-guide/run-app-in-kyma/manual-deployment/README.md#configmap)
    - [Service Deployment](documentation/step-by-step-guide/run-app-in-kyma/manual-deployment/README.md#service-deployment)
      - [S4 Service](documentation/step-by-step-guide/run-app-in-kyma/manual-deployment/README.md#bp-service)
        - [Maven build:](documentation/step-by-step-guide/run-app-in-kyma/manual-deployment/README.md#maven-build)
        - [Build the docker image](documentation/step-by-step-guide/run-app-in-kyma/manual-deployment/README.md#build-the-docker-image)
        - [Push the docker image to your docker repository](documentation/step-by-step-guide/run-app-in-kyma/manual-deployment/README.md#push-the-docker-image-to-your-docker-repository)
        - [Deploy the service](documentation/step-by-step-guide/run-app-in-kyma/manual-deployment/README.md#deploy-the-service)
      - [Easy Franchise Service](documentation/step-by-step-guide/run-app-in-kyma/manual-deployment/README.md#easy-franchise-service)
      - [DB Service](documentation/step-by-step-guide/run-app-in-kyma/manual-deployment/README.md#db-service)
  - [Email Service](documentation/step-by-step-guide/run-app-in-kyma/manual-deployment/README.md#email-service)
    - [Service Deployment](documentation/step-by-step-guide/run-app-in-kyma/manual-deployment/README.md#service-deployment-1)
  - [Frontend Deployment](documentation/step-by-step-guide/run-app-in-kyma/manual-deployment/README.md#frontend-deployment)
    - [Service Deployment](documentation/step-by-step-guide/run-app-in-kyma/manual-deployment/README.md#service-deployment-2)
  - [AppRouter Deployment](documentation/step-by-step-guide/run-app-in-kyma/manual-deployment/README.md#approuter-deployment)
  - [Broker Deployment](documentation/step-by-step-guide/run-app-in-kyma/manual-deployment/README.md#broker-deployment)
    - [Service Deployment](documentation/step-by-step-guide/run-app-in-kyma/manual-deployment/README.md#service-deployment-3)

documentation/step-by-step-guide/run-app-in-kyma/script-deployment

- [Deployment via Scripts](documentation/step-by-step-guide/run-app-in-kyma/script-deployment/README.md#deployment-via-scripts)
  - [Preparation](documentation/step-by-step-guide/run-app-in-kyma/script-deployment/README.md#preparation)
  - [Deployment sequence](documentation/step-by-step-guide/run-app-in-kyma/script-deployment/README.md#deployment-sequence)
  - [Un-deployment sequence](documentation/step-by-step-guide/run-app-in-kyma/script-deployment/README.md#un-deployment-sequence)

documentation/step-by-step-guide/run-app-in-kyma/update-ui-config

- [Update UI configuration](documentation/step-by-step-guide/run-app-in-kyma/update-ui-config/README.md#update-ui-configuration)

documentation/step-by-step-guide/run-app-locally

- [Run the EasyFranchise Application Locally](documentation/step-by-step-guide/run-app-locally/README.md#run-the-easyfranchise-application-locally)

documentation/step-by-step-guide/run-app-locally/run-email-service

- [Run the Email Service locally](documentation/step-by-step-guide/run-app-locally/run-email-service/README.md#run-the-email-service-locally)
  - [Prerequisites](documentation/step-by-step-guide/run-app-locally/run-email-service/README.md#prerequisites)
  - [Configure Properties](documentation/step-by-step-guide/run-app-locally/run-email-service/README.md#configure-properties)
  - [Build](documentation/step-by-step-guide/run-app-locally/run-email-service/README.md#build)
  - [Start the Service](documentation/step-by-step-guide/run-app-locally/run-email-service/README.md#start-the-service)
  - [Test the service](documentation/step-by-step-guide/run-app-locally/run-email-service/README.md#test-the-service)

documentation/step-by-step-guide/run-app-locally/run-ui

- [Set up development tools](documentation/step-by-step-guide/run-app-locally/run-ui/README.md#set-up-development-tools)
- [Run user interface locally](documentation/step-by-step-guide/run-app-locally/run-ui/README.md#run-user-interface-locally)
- [Test User Interface](documentation/step-by-step-guide/run-app-locally/run-ui/README.md#test-user-interface)

documentation/step-by-step-guide/run-app-locally/start-and-test-microservices

- [Start and Test the Microservices DBService, S4Service and EFService](documentation/step-by-step-guide/run-app-locally/start-and-test-microservices/README.md#start-and-test-the-microservices-dbservice-s4service-and-efservice)
  - [Prerequisites](documentation/step-by-step-guide/run-app-locally/start-and-test-microservices/README.md#prerequisites)
  - [Configure Properties](documentation/step-by-step-guide/run-app-locally/start-and-test-microservices/README.md#configure-properties)
  - [Build](documentation/step-by-step-guide/run-app-locally/start-and-test-microservices/README.md#build)
  - [Start all Services](documentation/step-by-step-guide/run-app-locally/start-and-test-microservices/README.md#start-all-services)
  - [Executing the First Admin Rest Call](documentation/step-by-step-guide/run-app-locally/start-and-test-microservices/README.md#executing-the-first-admin-rest-call)
  - [Onboard the First Tenant](documentation/step-by-step-guide/run-app-locally/start-and-test-microservices/README.md#onboard-the-first-tenant)
  - [Getting Franchises](documentation/step-by-step-guide/run-app-locally/start-and-test-microservices/README.md#getting-franchises)
  - [Create a Mentor](documentation/step-by-step-guide/run-app-locally/start-and-test-microservices/README.md#create-a-mentor)
  - [Assign Mentor to a Franchisee](documentation/step-by-step-guide/run-app-locally/start-and-test-microservices/README.md#assign-mentor-to-a-franchisee)


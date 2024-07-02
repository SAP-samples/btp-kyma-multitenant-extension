# Using btp-setup-automator to Prepare Environment and Application

In this section, we are explaining to use the [btp-setup-automator](https://github.com/SAP-samples/btp-setup-automator) to set up the account and run the application in an automated way.

>**Warning:** In the chapter [Instructions for running SAP Discovery Center Mission in btp-setup-automator](https://github.com/SAP-samples/btp-setup-automator/blob/main/usecases/released/discoverycenter/3638-kyma-multitenant/README.md#instructions) of the [btp-setup-automator](https://github.com/SAP-samples/btp-setup-automator), there is a small error in step 1 when navigating to the parameter file. The correct path is ```cd usecases/released/discoverycenter/3638-kyma-multitenant```
## Prerequisites
To install the usecase using the btp-setup-automator, you will need Docker. If you haven't it install, you can download it from [Docker Hub](https://hub.docker.com/).

To verify your installation, type the following in a shell:

```shell
docker version
```

The output should look like this:

```shell
Client: Docker Engine - Community
 Cloud integration: 1.0.17
 Version:           20.10.8
 API version:       1.41
 Go version:        go1.16.6
 Git commit:        3967b7d
 Built:             Fri Jul 30 19:54:02 2021
 OS/Arch:           linux/amd64
 Context:           default
 Experimental:      true

Server: Docker Engine - Community
 Engine:
  Version:          20.10.8
  API version:      1.41 (minimum version 1.12)
  Go version:       go1.16.6
  Git commit:       75249d8
  Built:            Fri Jul 30 19:52:10 2021
  OS/Arch:          linux/amd64
  Experimental:     false
 containerd:
  Version:          1.4.9
  GitCommit:        e25210fe30a0a703442421b0f60afac609f950a3
 runc:
  Version:          1.0.1
  GitCommit:        v1.0.1-0-g4144b63
 docker-init:
  Version:          0.19.0
  GitCommit:        de40ad0
```

## Configuring the SAP BTP environment
You can find the instructions how to prepare the account in the [use case for our mission](https://github.com/SAP-samples/btp-setup-automator/blob/main/usecases/released/discoverycenter/3638-kyma-multitenant/README.md).

## Configure the destination
Having everything in place, one final step is required from your side as it can't be automated. In the SAP BTP subaccount of the customer, you need to configure the details of the backend system used by the customer. For testing purpose we recommend to use the Business Partner Mock Server provided by us as this is also automatically by the btp-setup-automator. If you want to experiment it with your own S4/HANA Cloud system, this is of course possible to change the details. 

1. Verify first that you are in the correct customer subaccount (here *City Scooter*) and choose **Connectivity** on the left sidebar.

   ![](images/go-to-connectivity.png)

2. Choose **Destinations** under Connectivity and create a new destination by choosing **New Destination**.

   ![](images/create-destination-01.png)

3. Enter the following details to configure the destination pointing to the SAP S/4HANA system and then choose **Save**. Those details need of course to be adapted based on your system.

   If you are using the Business Partner Mock Server, insert the following details:
    * Name: EasyFranchise-S4HANA (here it's important to keep this name)
    * Type: HTTP
    * Description: Destination to Business Partner Mock Server
    * URL: http://business-partner-mock.mock:8081
    * Proxy Type: Internet
    * Authentication: NoAuthentication

    In case you want to use the SAP S/4HANA system, you need a **Communication Arrangement User** with the right permission for  `https://<your S/4 hostname>-api.s4hana.ondemand.com/sap/opu/odata/sap/API_BUSINESS_PARTNER`. See section [Enable the Business Partner OData V2 Rest API in an SAP S/4HANA Cloud System](../../../documentation/appendix/enable-odata-of-s4hana/README.md) if you need more details.
    * Name: EasyFranchise-S4HANA (here it's important to keep this name)
    * Type: HTTP
    * Description: Destination to SAP S/4HANA system
    * URL: https://my******-api.s4hana.ondemand.com (the path `/sap/opu/odata/sap/API_BUSINESS_PARTNER` will be added automatically by the application.)
    * Proxy Type: Internet
    * Authentication: BasicAuthentication
    * User and Password: Communication Arrangement User

   ![](images/create-destination-02.png)

4. Choose **Check Connection** and verify that the connection has been established. Then close the pop-up window. Please note if you used the mock server in your destination the check will fail. Reason for that is that the mock server is only reachable from within the Kyma Cluster, so it will work for the appliaction despite the check fails.

   ![](images/create-destination-03.png)

## Run the Application
That is all you need to do to run the application. Now you can test the application by starting it. More details can be found [here](../../../documentation/test-customer-onboarding/run-application/README.md).

# Deploy the Artifacts Manually <!-- omit in toc -->

This section explains how to deploy each component manually to the Kyma cluster. The following steps give an overview of the components discussed.

- [Deploy the Approuter](#deploy-the-approuter)
- [Deploy the Broker](#deploy-the-broker)
- [Deploy the Backend Components](#deploy-the-backend-components)
  - [Deploy the Configmap](#deploy-the-configmap)
  - [Run Maven Build](#run-Maven-build)
  - [Deploy the Business Partner Service](#deploy-the-business-partner-service)
  - [Deploy the Easy Franchise Service](#deploy-the-easy-franchise-service)
  - [Deploy the Database Service](#deploy-the-database-service)
- [Deploy Email Service](#deploy-email-service)
- [Deploy the UI](#deploy-the-ui)

## Deploy the Approuter

The Approuter deployment is done through two steps. The first step creates the required instances to XSUAA and the subscription service. The second step builds and deploy the actual Approuter image.

1. Run the script [checkActiveSubscription.sh](/code/approuter/checkActiveSubscription.sh) in folder `code/approuter` to make sure that there is no active subscription:

   ```shell
   > ./checkActiveSubscription.sh
   ```

   If there are no subscriptions the response should be like this:

   ```
   Checking active subscription in cluster  shoot--kyma--c-84e9cbb  of region eu10
   {"subscriptions":[]}
   0
   ```

2. Kubernetes artifacts for XSUAA service, SaaS Registry service, and Destination service are defined in [deployment-service.yaml](/code/approuter/k8s/deployment_service.yaml). Adapt the following values in the file:

   - `<cluster-domain>` must be replaced with the domain of your Kyma cluster, which can be taken from the console URL for instance. (For example, if the console URL is `console.c-97d8b1a.kyma.shoot.live.k8s-hana.ondemand.com`, then the cluster domain equals to `c-97d8b1a.kyma.shoot.live.k8s-hana.ondemand.com`.)

   - `<provider-subdomain>` must be replaced with the subdomain of the sub account where the application should be deployed to. This can be found in the SAP BTP cockpit.

     ![](images/subdomain.png)

3. You can proceed with the deployment to the Kyma cluster. Navigate to folder [code/approuter](/code/approuter):

   ```shell
   kubectl apply -f ./k8s/deployment_service.yaml
   ```

4. Continue with the deployment of the Approuter itself. You need to build the Docker image and push it to the repository:

   ```shell
   docker build --no-cache=true --rm -t <docker-repository>:approuter-0.1  -f ./docker/Dockerfile .
   docker push <docker-repository>:approuter-0.1
   ```

5. Before you can deploy the Approuter to the cluster, you need to adapt the [deployment.yaml](/code/approuter/k8s/deployment.yaml) and replace the following placeholders:

   - `<image-name>` the image name you just created
   - `<cluster-domain>` same as above
   - `<provider-subdomain>` same as above

6. After that you can deploy:

   ```shell
   kubectl apply -f ./k8s/deployment.yaml
   ```

7. Run the following command to verify that the deployment was successful:

   ```
   kubectl get pods -n integration
   ```

   You can find your newly deployed Approuter using the **Kyma dashboard**. Open it and select the namespace **integration** and open its**Pods**. Here you should now find an **approuter-xxxx**.

## Deploy the Broker

The SaaS Broker handles the on- and offboarding of a new tenant subscription.

1. Navigate to folder [code/broker](/code/broker) to build and push the Docker image:

   ```shell
   docker build --no-cache=true --rm -t <docker-repository>:broker-0.1  -f ./docker/Dockerfile .
   docker push <docker-repository>:broker-0.1
   ```

2. Before you can deploy the image, you need to replace the `<image-name>` tag in the deployment file with `<docker-repository>:broker-0.1`:

   ```shell
   kubectl apply -f ./k8s/deployment.yaml
   ```

3. Verify that you can find a new pod with the name **broker-xxxx** in the namespace **integration** of the Kyma cluster to validate the deployment.

## Deploy the Backend Components

The complete deployment of the backend consists of three microservices, one configmap, and a secret. The following section explains how to deploy the individual parts and what they do. In general all services follow the same pattern:

1. You need to build the project.
2. You need to build the docker image using the **Dockerfile** stored in the **docker** folder.
3. You need to push the build image to the docker repository.
4. You need to deploy the image to our Kubernetes cluster using the deployment descriptor stored in the **k8s** folder.

### Deploy the Configmap

1. Navigate to folder `code/backend` to execute the following commands.

2. To store configuration, you make use of a configmap called backend-configmap. The configmap stores the endpoints of the microservices, enables, or disables the scheduler and contains the name of the destination used for the connection to the SAP S4/HANA system. See [backend-configmap.yaml](/code/backend/config/backend-configmap.yaml) for more details..

3. As the configmap is mounted in all the microservices, you need to deploy it to the namespaces **backend** and **integration**. You can deploy the configmap using these two commands from your terminal:

   ```shell
   kubectl apply -n backend -f ./config/backend-configmap.yaml
   kubectl apply -n integration -f ./config/backend-configmap.yaml
   ```

   If the command was successful, the output should look like this:

   ```shell
   configmap/backend-configmap created
   ```

### Run Maven Build

The Business Partner service, the Database service, and the Easy Franchise service are Maven-based. Before you can deploy them, you have to make sure that you build the whole Java project at least once.

1. Navigate to folder [code/backend](/code/backend) and build the project by running the following command:

   ```shell
   mvn clean install
   ```

2. If the build was successful, the output should look like this:

   ```shell
   [INFO] ------------------------------------------------------------------------
   [INFO] Reactor Summary for easyfranchise 1.0-SNAPSHOT:
   [INFO]
   [INFO] easyfranchise ...................................... SUCCESS [  0.488 s]
   [INFO] shared-code ........................................ SUCCESS [ 24.740 s]
   [INFO] db-service ......................................... SUCCESS [ 14.799 s]
   [INFO] bp-service ......................................... SUCCESS [ 11.989 s]
   [INFO] ef-service ......................................... SUCCESS [ 13.574 s]
   [INFO] ------------------------------------------------------------------------
   [INFO] BUILD SUCCESS
   [INFO] ------------------------------------------------------------------------
   [INFO] Total time:  58.036 s
   [INFO] Finished at: 2021-09-23T11:51:09+02:00
   [INFO] ------------------------------------------------------------------------
   ```

### Deploy the Business Partner Service

1. Navigate to subfolder [code/backend/bp-service](/code/backend/bp-service) and build the docker image:

   ```shell
   docker build --no-cache=true --rm -t <docker-repository>:bp-service-0.1  -f ./docker/Dockerfile .
   ```

   If the build was successful, the output should look like this:

   ```shell
     [+] Building 7.5s (10/10) FINISHED
     => [internal] load build definition from Dockerfile                0.2s
     => => transferring dockerfile: 438B                                0.0s
     => [internal] load .dockerignore                                   0.1s
     => => transferring context: 2B                                     0.0s
     => [internal] load metadata for docker.io/library/sapmachine:16    4.4s
     => [auth] library/sapmachine:pull token for registry-1.docker.io   0.0s
     => [1/4] FROM docker.io/library/sapmachine:16@sha256:6faf1bebba... 0.0s
     => [internal] load build context                                   2.0s
     => => transferring context: 37.95MB                                2.0s
     => CACHED [2/4] WORKDIR /var/app                                   0.0s
     => [3/4] ADD target/bp-service.jar /var/app/bp-service.jar         0.1s
     => [4/4] ADD target/dependency/* /var/app/                         0.2s
     => exporting to image                                              0.5s
     => => exporting layers                                             0.4s
     => => writing image sha256:3ccf24626b5c9e9e16627583bb53ffbedd29... 0.0s
     => => naming to docker.io/easyfranchise/kyma-multitentant:bp-service-0.1
   ```

2. Execute the following command to push the Docker image to your Docker repository:

   ```shell
   docker push <docker-repository>:bp-service-0.1
   ```

   If the push was successful, the output should look like this:

   ```shell
   The push refers to repository [docker.io/easyfranchise/kyma-multitentant]
   0cf50934d132: Pushed
   61142fb7d7a8: Pushed
   54e3917473c9: Layer already exists
   c10ac09adbb7: Layer already exists
   afa540012129: Layer already exists
   4942a1abcbfa: Layer already exists
   bp-service-0.1: digest: sha256:fc995e1040cd4c38b331f3c8ce4989c7716734db815d5126d3890994a44309c8 size: 1580
   ```

3. Adapt the [deployment.yaml](/code/backend/bp-service/k8s/deployment.yaml) and enter the image name, that has been pushed. Search for <image-name> and enter the image name used in the step before `<docker-repository>:bp-service-0.1`.

4. Deploy the image using the following command:

   ```shell
   kubectl apply -f ./k8s/deployment.yaml
   ```

   If the deployment was successful, your output should look similar like this:

   ```shell
   deployment.apps/bp-service created
   service/bp-service created
   ```

### Deploy the Easy Franchise Service

1. Be sure you have built the project with Maven as described in the previous steps.

2. Navigate to folder [code/backend/ef-service](/code/backend/ef-service):

   ```shell
   docker build --no-cache=true --rm -t <docker-repository>:ef-service-0.1  -f ./docker/Dockerfile .
   docker push <docker-repository>:ef-service-0.1
   ```

3. Before you can deploy the image, you need to replace the <image-name> tag in the deployment file with `<docker-repository>:ef-service-0.1`.

4. Deploy the service:

   ```shell
   kubectl apply -f ./k8s/deployment.yaml
   ```

### Deploy the Database Service

1. Be sure to have built the project with Maven as described above.

2. Navigate to folder [code/backend/db-service](/code/backend/db-service):

    ```shell
    docker build --no-cache=true --rm -t <docker-repository>:db-service-0.1  -f ./docker/Dockerfile .
    docker push <docker-repository>:db-service-0.1
    ```

3. Before you can deploy the image, you need to replace the <image-name> tag in the deployment file with `<docker-repository>:db-service-0.1`.

4. Deploy the service:

    ```shell
    kubectl apply -f ./k8s/deployment.yaml
    ```

## Deploy the Email Service

The Email service uses only a secret to store the credentials for the Gmail account used to send notifications. Similar to the backend, the secret is not part of the repository and needs to be created like described in common tasks.

As the Email service is based on Node.js, there is no need to build the service beforehand.

1. You start with building and pushing the Docker image. Navigate to folder [code/email-service](/code/email-service) and run the following commands:

   ```shell
   docker build --no-cache=true --rm -t <docker-repository>:emailservice-0.1  -f ./docker/Dockerfile .
   docker push <docker-repository>:emailservice-0.1
   ```

2. Before you can deploy the image, you need to replace the <image-name> tag in the deployment file with `<docker-repository>:emailservice-0.1`.

3. Deploy the service:

   ```shell
   kubectl apply -f ./k8s/deployment.yaml
   ```

## Deploy the UI

Before deploying the UI, you need to check the global variable **backendApi**, which is the common path for all API. In section [Test the Easy Franchise Application Locally](/documentation/prepare/test-app-locally#3-run-user-interface-locally), this variable has been updated to work with your local services.

1. To use the deployed services, open the file [main.js](/code/ui/src/main.js).

2. Scroll down to the right place in the code and change the variable **backendApi** as follows:

   ```
   // Defining the url of the backend apis
   Vue.prototype.$backendApi = "/backend/easyfranchise/rest/efservice/v1";
   ```

4. As the UI does not need any kind of secrets nor configmaps, you can directly proceed with its deployment. The UI will be deployed in the **Frontend** namespace, which is defined in the namespace attribute of the `deployment.yaml`. To start building the Docker image, navigate to folder [code/ui](/code/ui) and run the following command:

   ```bash
   docker build --no-cache=true --rm -t <docker-repository>:ui-0.1  -f ./docker/Dockerfile .
   docker push <docker-repository>:ui-0.1
   ```

5. Before you can deploy the image, you need to replace the <image-name> tag in the deployment file with `<docker-repository>:ui-0.1`.

6. Deploy the service:

   ```bash
   kubectl apply -f ./k8s/deployment.yaml
   ```

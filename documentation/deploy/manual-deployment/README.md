# Deploy the Artifacts Manually

This section explains how to deploy each components manually to Kyma cluster.   Following list gives an overview of the components discussed.

* [Approuter Deployment](#approuter-deployment)
* [Broker Deployment](#broker-deployment)
* [Backend Deployment](#backend-deployment)
  * Configmap
  * BP service
  * Easy Franchise service
  * DB service
* [Email Service](#email-service)
* [Frontend Deployment](#frontend-deployment)

## Approuter Deployment

The Approuter deployment is done through two steps.  The first step creates the required instances to XSUAA and the subscription service. The second step builds and deploy the actual Approuter image. 

1. So we start with the deployment that creates the needed services. It's vital to make sure that there is no active subscription by running script [checkActiveSubscription.sh](/code/approuter/checkActiveSubscription.sh).  Following example shows there is no active subscription:

```shell
> ./checkActiveSubscription.sh
Checking active subscription in cluster  shoot--kyma--c-84e9cbb  of region eu10
{"subscriptions":[]}
0
```

2. Kubernetes artifacts for XSUAA service, SaaS Registry service and Destination service are defined in [deployment-service.yaml](/code/approuter/k8s/deployment_service.yaml).  It is necessary to adapt the following values in the file before it can be deployed to Kyma cluster:

- `<cluster-domain>` must be replaced with the domain of your Kyma cluster which can be taken from the console url for instance (e.g. if the console url is console.c-97d8b1a.kyma.shoot.live.k8s-hana.ondemand.com, then the cluster domain equals to c-97d8b1a.kyma.shoot.live.k8s-hana.ondemand.com)

- `<provider-subdomain>` must be replaced with the subdomain of the sub account where the application should be deployed to. This can be found in the cockpit. 

![](images/subdomain.png)

3. After that we can proceed with the deployment to the Kyma cluster Navigate into the [code/approuter](/code/approuter):

```shell
kubectl apply -f ./k8s/deployment_service.yaml
```

4. Now continue with the deployment of the Approuter itself. We need to build the docker image and push it to the repository:

```shell
docker build --no-cache=true --rm -t <docker-repository>:approuter-0.1  -f ./docker/Dockerfile .
docker push <docker-repository>:approuter-0.1
```

5. Before we can deploy the Approuter to the cluster we need to adapt [deployment.yaml](/code/approuter/k8s/deployment.yaml) and replace the following placeholders:

- `<image-name>` the image name you just created
- `<cluster-domain>` same as above
- `<provider-subdomain>` same as above

6. After that we can do the deployment:

```shell
kubectl apply -f ./k8s/deployment.yaml
```

If the deployment was successful you should see the Approuter deployment and pod running in the cluster.

## Broker Deployment

The next component is the SaaS Broker which handles on- and off-boarding of a new tenant subscription.

### Service Deployment 

We start with building the docker image:

1. Navigate to the [code/broker](/code/broker) folder to build and push the docker image:

```shell
docker build --no-cache=true --rm -t <docker-repository>:broker-0.1  -f ./docker/Dockerfile .
docker push <docker-repository>:broker-0.1
```

2. Before we can deploy the image we need to exchange the `<image-name>` tag in the deployment file with ``<docker-repository>:broker-0.1``

```shell
kubectl apply -f ./k8s/deployment.yaml
```

If the deployment was successful you can find the deployment and the new pod in the integration namespace of the cluster.

## Backend Deployment

1. The complete deployment of the backend consists of three microservices, one configmap and a secret. The follow section explains how to deploy the individual parts and what they do. Navigate to folder ``/code/backend` to execute the commands below.

### Configmap

2. In order to store configuration we make use of a config map called backend-configmap. The config map stores the endpoints of the microservices, enables or disables the scheduler and contains the name of the Destination used for the connection to the S4/HANA system. You can have a look at it [here](/code/backend/config/backend-configmap.yaml).

3. As the configmap is mounted into all our microservices it needs to be deployed to the "backend" as well as the "integration" namespace. You can deploy the configmap using this two commands from your terminal:

```shell
kubectl apply -n backend -f ./config/backend-configmap.yaml
kubectl apply -n integration -f ./config/backend-configmap.yaml
```

If the command was successful the output should look like the following:
```shell
configmap/backend-configmap created
```

### Service Deployment

After we have deployed the configmap and the secret (described in common tasks) we can continue with the deployment of the micro-services. In general all services follow the same pattern, first we need to build the maven project (Java based), second we need to build the docker image using the `Dockerfile` stored in the "docker" folder, third we need to push the build image to the docker repository and as last step we need to deploy the image to our Kubernetes cluster using the deployment descriptor stored in the "k8s" folder. 

#### BP Service

We start with the deployment of the BP Service. As this is the first service to be deployed,  we have to make sure that we build the whole java projects at least once.

##### Maven build:
4. Go to the [code/backend](/code/backend)  folder and build all projects using:

```shell
mvn clean install
```

If the build was successful the output should look like this: 
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

##### Build the docker image

5. Navigate into the [code/backend/bp-service](/code/backend/bp-service) subfolder and build the docker image:

```shell
docker build --no-cache=true --rm -t <docker-repository>:bp-service-0.1  -f ./docker/Dockerfile .
```

If the build was successful the output should look like this: 
```shell
[+] Building 7.5s (10/10) FINISHED
 => [internal] load build definition from Dockerfile                                                                                                                                                                      0.1s
 => => transferring dockerfile: 438B                                                                                                                                                                                      0.0s
 => [internal] load .dockerignore                                                                                                                                                                                         0.1s
 => => transferring context: 2B                                                                                                                                                                                           0.0s
 => [internal] load metadata for docker.io/library/sapmachine:16                                                                                                                                                          4.4s
 => [auth] library/sapmachine:pull token for registry-1.docker.io                                                                                                                                                         0.0s
 => [1/4] FROM docker.io/library/sapmachine:16@sha256:6faf1bebbac63db37ffa20cb06e3a13e636edf4505f271e0c22951a19c5ab292                                                                                                    0.0s
 => [internal] load build context                                                                                                                                                                                         2.0s
 => => transferring context: 37.95MB                                                                                                                                                                                      2.0s
 => CACHED [2/4] WORKDIR /var/app                                                                                                                                                                                         0.0s
 => [3/4] ADD target/bp-service.jar /var/app/bp-service.jar                                                                                                                                                               0.1s
 => [4/4] ADD target/dependency/* /var/app/                                                                                                                                                                               0.2s
 => exporting to image                                                                                                                                                                                                    0.5s
 => => exporting layers                                                                                                                                                                                                   0.4s
 => => writing image sha256:3ccf24626b5c9e9e16627583bb53ffbedd2901e6a90cb19bcede7bc61987609e                                                                                                                              0.0s
 => => naming to docker.io/easyfranchise/kyma-multitentant:bp-service-0.1
 ```

##### Push the docker image to your docker repository
6. Now execute the command to push the docker image to your docker repository
```shell
docker push <docker-repository>:bp-service-0.1
```

If the push was sucessful the output should look like this:

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

##### Deploy the service

7. Before we can deploy the service we need to adapt the [deployment.yaml](/code/backend/bp-service/k8s/deployment.yaml) and enter the imagename we have just pushed. Search for <image-name> and enter the image name used in the step before `<docker-repository>:bp-service-0.1`'. After that we can deploy the image to our cluster using the following command:

```shell
kubectl apply -f ./k8s/deployment.yaml
```

If the command was sucessful your output should look similar to the one below: 
```shell
deployment.apps/bp-service created
service/bp-service created
```

#### Easy Franchise Service

8. We need to repeat the steps which we did to deploy the BP Service. As we did a full build in the previous deployment we can skip that step. Navigate into the folder [code/backend/ef-service](/code/backend/ef-service)

```shell
docker build --no-cache=true --rm -t <docker-repository>:ef-service-0.1  -f ./docker/Dockerfile .
docker push <docker-repository>:ef-service-0.1
```

9. Before we can deploy the image we need to exchange the <image-name> tag in the deployment file with ``<docker-repository>:ef-service-0.1``

```shell
kubectl apply -f ./k8s/deployment.yaml
```

#### DB Service

10. We need to repeat the steps which we did to deploy the BP Service. As we did a full build in the previous deployment we can skip that step. Navigate into the folder [code/backend/db-service](/code/backend/db-service)

```shell
docker build --no-cache=true --rm -t <docker-repository>:db-service-0.1  -f ./docker/Dockerfile .
docker push <docker-repository>:db-service-0.1
```

11. Before we can deploy the image we need to exchange the <image-name> tag in the deployment file with ``<docker-repository>:db-service-0.1``

```shell
kubectl apply -f ./k8s/deployment.yaml
```

## Email Service

The Email Service uses only a secret to store the credentials for the gmail account used to send mails. Similar to the backend the secret is not part of the repository and needs to be created like described in common tasks.

### Service Deployment 

1. As the email service is based on node there is no need to build the service beforehand. This will be done automatically when we build our docker image.
So we start with building and pushing the docker image. Navigate to the [code/email-service](/code/email-service) folder.

```shell
docker build --no-cache=true --rm -t <docker-repository>:emailservice-0.1  -f ./docker/Dockerfile .
docker push <docker-repository>:emailservice-0.1
```

2. Before we can deploy the image we need to exchange the <image-name> tag in the deployment file with ``<docker-repository>:emailservice-0.1``

```shell
kubectl apply -f ./k8s/deployment.yaml
```

## Frontend Deployment
Before deploying the UI, we need to update the global variable used in the frontend to call the right API.

1. Open the file [main.js](/code/ui/src/main.js)
2. Scroll down to the section to define the backend APIs
3. Change the backend variable to: 

```
// Defining the url of the backend apis
Vue.prototype.$backendApi = "/backend/easyfranchise/rest/efservice/v1";
```

As the UI does not need any kind of secrets nor configmaps, we can directly proceed with the deployment of it. The UI itself will be deployed into the "Frontend" namespace, which is defined in the namespace attribute of the deployment.yaml.

### Service Deployment 
4. So we start with building and  the docker image. Navigate to the [code/ui](/code/backend/ui) folder:

```bash
docker build --no-cache=true --rm -t <docker-repository>:ui-0.1  -f ./docker/Dockerfile .
docker push <docker-repository>:ui-0.1
```

Before we can deploy the image we need to exchange the <image-name> tag in the deployment file with ``<docker-repository>:ui-0.1``

```bash
kubectl apply -f ./k8s/deployment.yaml
```

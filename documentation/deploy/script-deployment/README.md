# Deployment via Scripts

In the subfolder of each component, there is a shell script `build.sh` or `build_service.sh`.  Calling the script similar to below will deploy corresponding component to Kyma cluster.

```shell
# call build.sh script using envrionment variable defined in ../.env
./build.sh  -e=../.env

# call build.sh script in dryrun mode, only replacing place-holder with value defined in ..env-dev file. Dry-run will NOT deploy to Kyma cluster.
./build.sh  -e=../.env --dry-run
```

## Preparation 
If you run through the manual deployment before, revert the changes you have done to the ``.yaml`` files. The build scripts will modify them according to provided ``.env`` file.

Check that you adapt the values in the [code/.env-template](../../../../code/.env-template) file. See chapter [Prepare the Deployment](../prepare-deployment/README.md)

## Deployment sequence

If you plan to use deployment scripts, please adhere to following sequence.

1. Deploy XSUAA, SaaS Provision and Destination service:

   ```shell
   # execute in approuter subfolder
   
   # e.g. deploy using .env file ../.env
   
   ./build_service.sh --env=../.env
   
   # --dry-run option will print out the rendered yaml file after replacing all placeholders .e.g <provider-subdomain>, <cluster-domain>
   
   ./build_service.sh --env=../.env --dry-run
   ```
   Note that if there are already active subscription to the existing saas-registry service, the script will exit without deployment. Please make sure to first unsubscribe all tenants and try again.


2. Deploy Approuter

   ```shell
   # execute in approuter folder
   build.sh [--env=../.env]
   ```

3. Deploy backend

   Full deployment of all components
   ```shell
   ### execute in backend folder
   full-build.sh [--env=../.env]
   ```
   Individual deployment
   ```shell
   ### execute in each component folder (db-service, ef-service, bp-service)
   build.sh [--env=../.env]
   ```

4. Deploy broker 

   ```shell
   # execute in broker folder
   build.sh [--env=../.env]
   ```

5. Deploy ui

   ```shell
   # execute in ui folder
   build.sh [--env=../.env]
   ```

6. Email service

   ```shell
   ### execute in email-service folder
   build.sh [--env=../.env]
   ```


## Un-deployment sequence

1. *check if there is any active subscription*
   ```shell
   # execute 
   ./approuter/checkActiveSubscription.sh
   ```
   Make sure there is no active subscription in output. Otherwise please unsubscribe first.

2. Un-deploy ui

   ```shell
   # execute in ui folder
   kubectl -n frontend delete -f k8s/deployment.yaml
   ```

3. Un-deploy broker

   ```shell
   # execute in broker folder
   kubectl -n integration delete -f k8s/deployment.yaml
   ```

4. Un-deploy Approuter

   ```shell
   # execute in approuter folder
   kubectl -n integration delete -f k8s/deployment.yaml
   ```

5. Un-deploy Backend
   ```shell
   ### execute in folder: db-service and bp-service
   kubectl -n integration delete -f k8s/deployment.yaml
   
   ### execute in ef-service folder
   kubectl -n backend delete -f k8s/deployment.yaml
   
   ```

6. Delete all service instances (**xsuaa**, **saas-registry** and **destination**) and subscription APIRule


   ```shell
   # delete subscription apirule
   kubectl -n integration delete apirule subscription-apirule
   
   # delete destination service
   kubectl -n integration delete serviceinstances ef-destination-service
   
   # delete saas-registry service
   kubectl -n integration delete serviceinstances saas-registry-service
   
   # delete xsuaa service
   kubectl -n integration delete serviceinstances xsuaa-service
   ```
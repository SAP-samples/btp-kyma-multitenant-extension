#!/usr/bin/env bash
log() {
  # Print the input text in yellow.
  local yellow='\033[0;33m'
  local no_color='\033[0m'
  echo -e "${yellow}$*${no_color}"
}

echo
log "███████  █████  ███████ ██    ██     ███████ ██████   █████  ███    ██  ██████ ██   ██ ██ ███████ ███████ ";
log "██      ██   ██ ██       ██  ██      ██      ██   ██ ██   ██ ████   ██ ██      ██   ██ ██ ██      ██      ";
log "█████   ███████ ███████   ████       █████   ██████  ███████ ██ ██  ██ ██      ███████ ██ ███████ █████   ";
log "██      ██   ██      ██    ██        ██      ██   ██ ██   ██ ██  ██ ██ ██      ██   ██ ██      ██ ██      ";
log "███████ ██   ██ ███████    ██        ██      ██   ██ ██   ██ ██   ████  ██████ ██   ██ ██ ███████ ███████ ";
echo
echo
echo

programname=$0
DRY_RUN=false
COMPONENT_SELECTED=false
COMPONENT_NUMBER=9
USE_ARTIFACTORY=false
COLUMNS=12
configfile=easyfranchiseconfig.json

# for returning a single value:
declare retval=""

function usage {
    echo "usage: $programname [-e=<.env file> | --env=<.env file>] [-d | --dry-run] [-h]"
    echo "  -c=, --component=   ** optional **        specify the component to be deployed (1=Approuter, 2=SaaS Broker, 3=BP-Service, 4=DB-Service, 5=EF-Service, 6=Email Service, 7=UI, 8=Mock Server, 9=Full Deployment (default)"
    echo "  -d, --dry-run       ** optional **        print rendered yaml file without deploying to Kyma"    
    echo "  -h                  ** optional **        display help"
    exit 1
}

### read / write config file
write_config() {
  rawjson="{ \"subdomain-id\": \"$SUBDOMAIN\", \"cluster-domain\": \"$CLUSTER_DOMAIN\", \"kubeconfig-url\": \"$KUBECONFIG_URL\", \
   \"docker-email\": \"$DOCKER_EMAIL\", \"docker-id\": \"$DOCKER_ID\", \"docker-server\": \"$DOCKER_SERVER\", \"docker-repository\": \"$DOCKER_REPOSITORY\", \"docker-password\": \"$DOCKER_PASSWORD\", \
   \"db-sqlendpoint\": \"$DB_SQLENDPOINT\", \"db-admin\": \"$DB_ADMIN\", \"db-admin-password\": \"$DB_ADMIN_PASSWORD\"}"
  echo "$rawjson" | jq '.' >$configfile
}

read_config() {
  log "read configuration file"
  result=$(jq '.' $configfile)  
  SUBDOMAIN=$(jq -r '."subdomain-id"' <<< "${result}")
  CLUSTER_DOMAIN=$(jq -r '."cluster-domain"' <<< "${result}")
  KUBECONFIG_URL=$(jq -r '."kubeconfig-url"' <<< "${result}")

  #Docker Environment
  DOCKER_EMAIL=$(jq -r '."docker-email"' <<< "${result}")
  DOCKER_ID=$(jq -r '."docker-id"' <<< "${result}")
  DOCKER_SERVER=$(jq -r '."docker-server"' <<< "${result}")
  DOCKER_REPOSITORY=$(jq -r '."docker-repository"' <<< "${result}")
  DOCKER_PASSWORD=$(jq -r '."docker-password"' <<< "${result}")

  #HANA Cloud
  DB_SQLENDPOINT=$(jq -r '."db-sqlendpoint"' <<< "${result}")
  DB_ADMIN=$(jq -r '."db-admin"' <<< "${result}")
  DB_ADMIN_PASSWORD=$(jq -r '."db-admin-password"' <<< "${result}")
}

function buildDeploy() {  
  
  PROJECT=$1
  DOCKER_FILE=$2
  K8S_YAML=$3
  
  if [ "$USE_ARTIFACTORY" = "true" ]; then
    IMAGE_NAME="$DOCKER_REPOSITORY/$PROJECT:$VERSION"    
  else
    IMAGE_NAME="$DOCKER_REPOSITORY:$PROJECT-$VERSION"    
  fi
  log "$PROJECT Image Name: '$IMAGE_NAME' "
  echo
  if [ "$DRY_RUN" = false ]; then
    log "$PROJECT: Building new image"
    docker build --no-cache=true --rm -t "$IMAGE_NAME"  -f "$DOCKER_FILE" ./../..
    echo
    log "$PROJECT: Push new image"
    docker push "$IMAGE_NAME"
    echo
    log "$PROJECT: Deploy image"
    cat "$K8S_YAML" | sed "s~<image-name>~$IMAGE_NAME~g" | sed "s~<provider-subdomain>~$SUBDOMAIN~g" | sed "s~<cluster-domain>~$CLUSTER_DOMAIN~g" | kubectl apply -f -
    echo
  else
    log "Dry Run: Skipping build"
    log "$PROJECT: Render Template"
    cat "$K8S_YAML" | sed "s~<image-name>~$IMAGE_NAME~g" | sed "s~<provider-subdomain>~$SUBDOMAIN~g" | sed "s~<cluster-domain>~$CLUSTER_DOMAIN~g" | echo "$(cat -)"
    echo
  fi  
}

continue_prompt_bool() {
  log
  read -p "$1 (y or yes to accept): " -r varname
  if [[ "$varname" != "y" ]] && [[ "$varname" != "yes" ]];
  then
    retval=false
  else
    retval=true
  fi
}

query_parameters() {
 # BTP Environment
  log "Step 1.1 - BTP Environment"
  log "Enter Subdomain: " 
  read -r SUBDOMAIN

  log "Enter Cluster Domain: " 
  read -r CLUSTER_DOMAIN

  log "URL to Kubeconfig: " 
  read -r KUBECONFIG_URL
  echo ""

  # Docker Repository
  log "Step 1.2 - Docker Setup"
  log "Enter Docker Email: " 
  read -r DOCKER_EMAIL

  log "Enter Docker ID: " 
  read -r DOCKER_ID

  log "Enter Docker Password: " 
  read -s -r DOCKER_PASSWORD
  echo ""

  log "Enter Docker Server (e.g. https://index.docker.io/v1/ for Docker Hub): " 
  read -r DOCKER_SERVER

  log "Enter Docker Repository (e.g. for Docker Hub <docker account>/<repository name>): " 
  read -r DOCKER_REPOSITORY
  echo ""

  # HANA Cloud Setup
  log "Step 1.3 - HANA Cloud"

  log "Enter SQL Endpoint: " 
  read -r DB_SQLENDPOINT

  log "Enter DB Admin: " 
  read -r DB_ADMIN

  log "Enter DB Admin Password: " 
  read -s -r DB_ADMIN_PASSWORD
  echo ""
  echo ""
}

# user selection via passed array
# return ( index value)
createmenu() {
  #echo "Size of array: $#"
  #echo "$@"
  select option; do # in "$@" is the default
    if [ "$REPLY" -eq "$#" ];
    then
      #echo "Exiting..."
      break;
    elif [ 1 -le "$REPLY" ] && [ "$REPLY" -le $(($#-1)) ];
    then
      #echo "You selected $option which is option $REPLY"
      break;
    else
      echo "Incorrect Input: Select a number 1-$#"
    fi
  done
  retval=$REPLY  
}


for i in "$@"; do
  case $i in    
    -c=*|--component=*)
      COMPONENT_NUMBER="${i#*=}"
      COMPONENT_SELECTED=true
      shift # past argument=value
      ;;
    -d|--dry-run)
      DRY_RUN=true
      shift # past argument with no value
      ;;
    -a|--artifactory)
      USE_ARTIFACTORY=true
      shift # past argument with no value
      ;;
    -h|--help)
      usage
      ;;      
    *)
      # unknown option
      log "Error: Unknown argument ${i}"
      usage
      ;;
  esac
done

echo "================================================================================================="
echo "Step 1 - User Input"
echo "================================================================================================="
echo ""

FILE=./$configfile
if test -f "$FILE"; then
  continue_prompt_bool "Read parameters form config file config.json?"
  doit=$retval
  echo "$doit"
  if [ "$doit" = true ]; then
    read_config
   else
    query_parameters
    
    continue_prompt_bool "Save Attributes as file (Please note that password will be stored in plain text!!!)?"
    doit=$retval
    echo "$doit"
    if [ "$doit" = true ]; then
      write_config
    fi        
  fi
 else
  query_parameters
  continue_prompt_bool "Save Attributes as file (Please note that password will be stored in plain text!!!)?"
    doit=$retval
    echo "$doit"
    if [ "$doit" = true ]; then
      write_config
    fi
fi

# Summary Step 1
echo ""
log "Step 1 - Summary"
echo ""

log "Deployment will be performed with the given attributes:"
echo ""
log "BTP Environment"
log "Subdomain: " "$SUBDOMAIN"
log "Kyma Cluster Domain: " "$CLUSTER_DOMAIN"
log "Kubeconfig Url: " "$KUBECONFIG_URL"
echo ""
log "Docker Environment"
log "Docker E-Mail: " "$DOCKER_EMAIL"
log "Docker ID: " "$DOCKER_ID"
log "Docker Password: ***********" 
log "Docker Server: " "$DOCKER_SERVER"
log "Docker Repository: " "$DOCKER_REPOSITORY"
echo ""
log "HANA Cloud"
log "SQL Endpoint: " "$DB_SQLENDPOINT"
log "DB Admin: " "$DB_ADMIN"
log "DB Admin Password: ********"
echo ""
echo ""
read -p "Continue with deployment? (y/n) " -n 1 -r
echo ""   # (optional) move to a new line

if [[ $REPLY =~ ^[Yy]$ ]]; then
    set -e #Cause script to break if an error occurs
    
    #Choose components to be deployed
    if [ "$COMPONENT_SELECTED" = false ]; then
        echo
        log "Choose component for deployment or deploy the whole application"
        declare -a arr="(Approuter DB-Service BP-Service EF-Service SaaS-Broker Email-Service UI Mock-Server Full-Deployment)"    # must be quoted like this
        createmenu "${arr[@]}"
        COMPONENT_NUMBER="$retval"      
    fi

    log "================================================================================================="
    log "Step 2 - Check Kyma Environment"
    log "================================================================================================="
    echo ""
    log "Step 2.1 - Validate Cluster Access"
    log "Download Kubeconfig from $KUBECONFIG_URL"
    wget -q "$KUBECONFIG_URL" -O kubeconfig.yaml
    export KUBECONFIG="./kubeconfig.yaml"
    
    KYMA_CLUSTER_CONFIG="$(kubectl config view)"
    if [[ ${KYMA_CLUSTER_CONFIG} != *"$CLUSTER_DOMAIN"* ]];then
      log "Error: Check your configuration, current Kyma cluster does not match \033[1;31m $CLUSTER_DOMAIN \033[0m defined in provided .env file"
      exit 1
    fi
    log "Cluster Access successful"
    echo 
#    log "Step 2.2 - Check active subscriptions"
    # https://saas-manager.cfapps.eu10.hana.ondemand.com/api#/API%20order%20a/getApplicationSubscriptions
#
    #determine the kyma runtime region, e.g. eu10 or eu20
#    REGION="$(kubectl get ClusterServiceBroker | sed -nr 's/.*service-manager.cfapps.(.*).hana.*/\1/p' | head -n 1)"
#    log "Checking active subscription in cluster \033[1;31m $(kubectl config current-context) \033[0m of region $REGION"
#    CLIENT_ID="$(kubectl -n integration get secrets saas-registry-service-binding --ignore-not-found -o jsonpath='{.data.clientid}' | base64 --decode)"
#    CLIENT_SECRET="$(kubectl -n integration get secrets saas-registry-service-binding --ignore-not-found -o jsonpath='{.data.clientsecret}' | base64 --decode)"
#    URL="$(kubectl -n integration get secrets saas-registry-service-binding --ignore-not-found -o jsonpath='{.data.url}' | base64 --decode)"
#    TOKEN="$(curl -s -L -X POST "$URL/oauth/token" -H 'Content-Type: application/x-www-form-urlencoded' -u "$CLIENT_ID:$CLIENT_SECRET" -d 'grant_type=client_credentials' | jq -r '.access_token')"
    # check active subscription
    # echo "active subscription in region $REGION: "
    #curl  -H "Authorization: Bearer $TOKEN"   "https://saas-manager.cfapps.${REGION}.hana.ondemand.com/saas-manager/v1/application/subscriptions"
#    subscription=$(curl -sS -H "Authorization: Bearer $TOKEN"   "https://saas-manager.cfapps.${REGION}.hana.ondemand.com/saas-manager/v1/application/subscriptions")
#    echo "$subscription" | jq '.'
#    LENGTH="$(echo "$subscription" | jq '.subscriptions | length')"    
#    echo ""
#    if [ "$LENGTH" -gt 0 ]; then
#        read -p "There are active subscriptions in your subaccount. Please make sure that you did not change the saas registry or xsuaa service instance described in ef-service.yaml. Continue with deployment? (y/n) " -n 1 -r
#        if [[ $REPLY =~ ^[Nn]$ ]]; then
#          exit 1
#        fi
#    fi
#    echo
    echo

    log "================================================================================================="
    log "Step 3 - Prepare Cluster for Deployment"
    log "================================================================================================="
    echo ""

    log "Step 3.1 - Enable BTP Operator"
    if [ "$DRY_RUN" = false ]; then 
      # Download and Install Kyma CLI
      curl -Lo kyma.tar.gz "https://github.com/kyma-project/cli/releases/download/$(curl -s https://api.github.com/repos/kyma-project/cli/releases/latest | grep tag_name | cut -d '"' -f 4)/kyma_Linux_x86_64.tar.gz" \
      && mkdir kyma-release && tar -C kyma-release -zxvf kyma.tar.gz && chmod +x kyma-release/kyma && sudo mv kyma-release/kyma /usr/local/bin \
      && rm -rf kyma-release kyma.tar.gz

      # Wait for Kyma CLI to be ready
      KYMA_CLI_READY=0
      MAX_RETRIES=5
      RETRY_INTERVAL=5

      for ((i=1; i<=MAX_RETRIES; i++)); do
          if kyma version; then
              KYMA_CLI_READY=1
              break
          else
              echo "Waiting for Kyma CLI to be ready... retry $i/$MAX_RETRIES"
              sleep $RETRY_INTERVAL
          fi
      done

      if [ $KYMA_CLI_READY -eq 0 ]; then
          echo "Kyma CLI is not ready. Exiting..."
          exit 1
      else
          echo "Kyma CLI is ready."
      fi

      # Enable BTP Operator
      kyma alpha enable module btp-operator  --channel regular --kyma-name default --wait


      # Wait for BTP Operator to be ready
      BTP_OPERATOR_READY=0
      MAX_RETRIES=30
      RETRY_INTERVAL=10

      for ((i=1; i<=MAX_RETRIES; i++)); do
          if kubectl get pods -n kyma-system | grep -q 'btp-operator.*Running'; then
              BTP_OPERATOR_READY=1
              break
          else
              echo "Waiting for BTP Operator to be ready... retry $i/$MAX_RETRIES"
              sleep $RETRY_INTERVAL
          fi
      done

      if [ $BTP_OPERATOR_READY -eq 0 ]; then
          echo "BTP Operator is not ready. Exiting..."
          exit 1
      else
          echo "BTP Operator is ready."
      fi
    else 
      log "Skipped for Dry Run"
    fi

    echo
    log "Step 3.2 - Create Namepaces"
    if [ "$DRY_RUN" = false ]; then
      kubectl create namespace integration || true
      kubectl create namespace backend || true
      kubectl create namespace mock || true
      kubectl create namespace frontend || true

      kubectl label namespace integration istio-injection=enabled --overwrite || true
      kubectl label namespace backend istio-injection=enabled --overwrite || true
      kubectl label namespace mock istio-injection=enabled --overwrite || true
      kubectl label namespace frontend istio-injection=enabled --overwrite || true
    else 
      log "Skipped for Dry Run"
    fi

    echo
    log "Step 3.3 - Create Secrets, Configmap and depended Services"
    echo
    log "DB Secret: "
    if [ "$DRY_RUN" = true ]; then
      log 'DB Secret: Showing rendered yaml file'
      echo '-----------------------------------'
      cat ./../easyfranchise/deployment/k8s/db-secret.yaml | sed "s~<db-sqlendpoint>~$DB_SQLENDPOINT~g" | sed "s~<db-admin>~$DB_ADMIN~g" | sed "s~<db-admin-password>~$DB_ADMIN_PASSWORD~g" | echo "$(cat -)"
      echo '-----------------------------------'
    else 
      cat ./../easyfranchise/deployment/k8s/db-secret.yaml | sed "s~<db-sqlendpoint>~$DB_SQLENDPOINT~g" | sed "s~<db-admin>~$DB_ADMIN~g" | sed "s~<db-admin-password>~$DB_ADMIN_PASSWORD~g" | kubectl apply -f - || true
    fi
    echo
    log "Registry Secrets"

    if [ "$DRY_RUN" = true ]; then
      log "Skipped for Dry Run"
    else 
      kubectl -n mock  create secret docker-registry registry-secret --docker-server="$DOCKER_SERVER"  --docker-username="$DOCKER_ID" --docker-password="$DOCKER_PASSWORD" --docker-email="$DOCKER_EMAIL" || true
      kubectl -n integration  create secret docker-registry registry-secret --docker-server="$DOCKER_SERVER"  --docker-username="$DOCKER_ID" --docker-password="$DOCKER_PASSWORD" --docker-email="$DOCKER_EMAIL" || true
      kubectl -n frontend  create secret docker-registry registry-secret --docker-server="$DOCKER_SERVER"  --docker-username="$DOCKER_ID" --docker-password="$DOCKER_PASSWORD" --docker-email="$DOCKER_EMAIL" || true
      kubectl -n backend  create secret docker-registry registry-secret --docker-server="$DOCKER_SERVER"  --docker-username="$DOCKER_ID" --docker-password="$DOCKER_PASSWORD" --docker-email="$DOCKER_EMAIL" || true      
    fi
    echo 
    
    log "Backend Configmap"
    if [ "$DRY_RUN" = true ]; then
      log "Skipped for Dry Run"
    else 
      kubectl apply -n backend -f ./../easyfranchise/deployment/k8s/backend-configmap.yaml
      kubectl apply -n integration -f ./../easyfranchise/deployment/k8s/backend-configmap.yaml      
    fi
    echo

    log "Dependend Services"
    if [ "$DRY_RUN" = false ]; then 
      echo
      log "Deploy depended Services"
      cat ./../easyfranchise/deployment/k8s/btp-services.yaml | sed "s~<provider-subdomain>~$SUBDOMAIN~g" | sed "s~<cluster-domain>~$CLUSTER_DOMAIN~g" | kubectl apply -f -
      echo
    else 
      log "Dry Run: Depended Services"
      cat ./../easyfranchise/deployment/k8s/btp-services.yaml | sed "s~<provider-subdomain>~$SUBDOMAIN~g" | sed "s~<cluster-domain>~$CLUSTER_DOMAIN~g" | echo "$(cat -)"
    fi
    echo
    
    log "================================================================================================="
    log "Step 4 - Docker Setup"
    log "================================================================================================="
    echo ""
    docker login "$DOCKER_SERVER" -u "$DOCKER_ID" -p "$DOCKER_PASSWORD"
    echo

    log "================================================================================================="
    log "Step 5 - Component Build and Deploy"
    log "================================================================================================="
    echo ""
    
    VERSION=$(uuidgen)

    case $COMPONENT_NUMBER in
      1|9)	
      PROJECT=approuter
      log "Step 5.1 - $PROJECT: Building, Push and Deploy"
      buildDeploy $PROJECT ./../easyfranchise/deployment/docker/Dockerfile-$PROJECT ./../easyfranchise/deployment/k8s/$PROJECT.yaml

      ;;&    
      2|9)
      PROJECT=db-service
      log "Step 5.2 - $PROJECT: Building, Push and Deploy"
      buildDeploy $PROJECT ./../easyfranchise/deployment/docker/Dockerfile-$PROJECT ./../easyfranchise/deployment/k8s/$PROJECT.yaml
      ;;&
      3|9)
      PROJECT=bp-service
      log "Step 5.3 - $PROJECT: Building, Push and Deploy"
      buildDeploy $PROJECT ./../easyfranchise/deployment/docker/Dockerfile-$PROJECT ./../easyfranchise/deployment/k8s/$PROJECT.yaml
      ;;&
      4|9)
      PROJECT=ef-service
      log "Step 5.4 - $PROJECT: Building, Push and Deploy"
      buildDeploy $PROJECT ./../easyfranchise/deployment/docker/Dockerfile-$PROJECT ./../easyfranchise/deployment/k8s/$PROJECT.yaml
      ;;&
      5|9)
      PROJECT=broker
      log "Step 5.5 - $PROJECT: Building, Push and Deploy"
      buildDeploy $PROJECT ./../easyfranchise/deployment/docker/Dockerfile-$PROJECT ./../easyfranchise/deployment/k8s/$PROJECT.yaml
      ;;&
      6|9)
      PROJECT=email-service
      log "Step 5.6 - $PROJECT: Building, Push and Deploy"
      buildDeploy $PROJECT ./../easyfranchise/deployment/docker/Dockerfile-$PROJECT ./../easyfranchise/deployment/k8s/$PROJECT.yaml
      ;;&
      7|9)
      PROJECT=ui
      log "Step 5.7 - $PROJECT: Building, Push and Deploy"
      buildDeploy $PROJECT ./../easyfranchise/deployment/docker/Dockerfile-$PROJECT ./../easyfranchise/deployment/k8s/$PROJECT.yaml
      ;;&
      8|9)
      PROJECT=business-partner-mock
      log "Step 5.8 - $PROJECT: Building, Push and Deploy"
      buildDeploy $PROJECT ./../easyfranchise/deployment/docker/Dockerfile-$PROJECT ./../easyfranchise/deployment/k8s/$PROJECT.yaml
      ;;      
    esac

    echo
    log "================================================================================================="
    log "Deployment Successful"
    log "================================================================================================="
    echo
fi 
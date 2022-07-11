#!/usr/bin/env bash

log() {
  # Print the input text in yellow.
  local yellow='\033[0;33m'
  local no_color='\033[0m'
  echo -e "${yellow}$*${no_color}"
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

read_automator_config() {  
  result=$(jq '.' /home/user/log/metadata_log.json)
  SUBDOMAIN=$(jq -r '."subdomain"' <<< "${result}")
  CLUSTER_DOMAIN=$(jq -r '."kymaDashboardUrl"' <<< "${result}")
  CLUSTER_DOMAIN="${CLUSTER_DOMAIN#*//console.}"
  KUBECONFIG_URL=$(jq -r '."kymaKubeConfigUrl"' <<< "${result}")
  DB_ADMIN="DBADMIN"
  DB_ADMIN_PASSWORD="$( echo "$result" | jq -r '.createdServiceInstances[] | select(.name == "hana-cloud") | .parameters.data.systempassword' 2> /dev/null)"
  
  DB_DASHBOARD="$( echo "$result" | jq -r '.createdServiceInstances[] | select(.name == "hana-cloud") | .statusResponse | ."dashboard url" ' 2> /dev/null)"  
  DB_HOST="${DB_DASHBOARD#*?host=}"
  DB_HOST=${DB_HOST/&component*}
  DB_PORT=${DB_DASHBOARD/*port=/}
  DB_SQLENDPOINT="$DB_HOST:$DB_PORT"
}

echo "####################################################################################################"
echo "# Step 1 - Summary of Application Environment"
echo "####################################################################################################"
echo ""
read_automator_config

# Summary Step 1
echo
log "Deployment will be performed with the following attributes:"
echo ""
log "BTP Environment"
log "Provider Subdomain: " "$SUBDOMAIN"
log "Kyma Cluster Domain: " "$CLUSTER_DOMAIN"
log "Kubeconfig Url: " "$KUBECONFIG_URL"
echo ""
log "HANA Cloud"
log "SQL Endpoint: " "$DB_SQLENDPOINT"
log "DB Admin: " "$DB_ADMIN"
log "DB Admin Password: ********"
echo ""
echo ""

read -p "Continue with deployment? (y/n) " -n 1 -r
echo 
echo

if [[ $REPLY =~ ^[Yy]$ ]]; then

  log "####################################################################################################"
  log "# Step 2 - Deployment of Application Components"
  log "####################################################################################################"
  echo ""

  log "Step 2.1 - Create Namepaces"  
  kubectl create namespace integration || true
  kubectl create namespace backend || true
  kubectl create namespace mock || true
  kubectl create namespace frontend || true
  echo

  log "Step 2.2 - DB Secret: "
  cat /home/user/tutorial/code/easyfranchise/deployment/k8s/db-secret.yaml | sed "s~<db-sqlendpoint>~$DB_SQLENDPOINT~g" | sed "s~<db-admin>~$DB_ADMIN~g" | sed "s~<db-admin-password>~$DB_ADMIN_PASSWORD~g" | kubectl apply -f - || true
  echo

  log "Step 2.3 - Backend Configmap"
  kubectl apply -n backend -f /home/user/tutorial/code/easyfranchise/deployment/k8s/backend-configmap.yaml
  kubectl apply -n integration -f /home/user/tutorial/code/easyfranchise/deployment/k8s/backend-configmap.yaml      
  echo

  log "Step 2.4 - BTP Service Deployment"
  cat "/home/user/tutorial/code/easyfranchise/deployment/k8s/btp-services.yaml" | sed "s~<provider-subdomain>~$SUBDOMAIN~g" | sed "s~<cluster-domain>~$CLUSTER_DOMAIN~g" | kubectl apply -f -
  echo

  PROJECT=approuter
  log "Step 2.5 - Deploy $PROJECT"
  cat "/home/user/tutorial/code/easyfranchise/deployment/k8s/$PROJECT.yaml" | sed "s~<image-name>~$BTPSA_KYMA_IMAGE_NAME_APPROUTER~g" | sed "s~<provider-subdomain>~$SUBDOMAIN~g" | sed "s~<cluster-domain>~$CLUSTER_DOMAIN~g" | kubectl apply -f -

  PROJECT=db-service
  log "Step 2.6 - Deploy $PROJECT"
  cat "/home/user/tutorial/code/easyfranchise/deployment/k8s/$PROJECT.yaml" | sed "s~<image-name>~$BTPSA_KYMA_IMAGE_NAME_DB_SERVICE~g" | kubectl apply -f -

  PROJECT=bp-service
  log "Step 2.7 - Deploy $PROJECT"
  cat "/home/user/tutorial/code/easyfranchise/deployment/k8s/$PROJECT.yaml" | sed "s~<image-name>~$BTPSA_KYMA_IMAGE_NAME_BP_SERVICE~g" | kubectl apply -f -

  PROJECT=ef-service
  log "Step 2.8 - Deploy $PROJECT"
  cat "/home/user/tutorial/code/easyfranchise/deployment/k8s/$PROJECT.yaml" | sed "s~<image-name>~$BTPSA_KYMA_IMAGE_NAME_EF_SERVICE~g" | kubectl apply -f -

  PROJECT=broker
  log "Step 2.9 - Deploy $PROJECT"
  cat "/home/user/tutorial/code/easyfranchise/deployment/k8s/$PROJECT.yaml" | sed "s~<image-name>~$BTPSA_KYMA_IMAGE_NAME_BROKER~g" | kubectl apply -f -

  PROJECT=email-service
  log "Step 2.10 - Deploy $PROJECT"
  cat "/home/user/tutorial/code/easyfranchise/deployment/k8s/$PROJECT.yaml" | sed "s~<image-name>~$BTPSA_KYMA_IMAGE_NAME_EMAIL_SERVICE~g" | kubectl apply -f -

  PROJECT=ui
  log "Step 2.11 - Deploy $PROJECT"
  cat "/home/user/tutorial/code/easyfranchise/deployment/k8s/$PROJECT.yaml" | sed "s~<image-name>~$BTPSA_KYMA_IMAGE_NAME_UI~g" | kubectl apply -f -

  PROJECT=business-partner-mock
  log "Step 2.12 - Deploy $PROJECT"
  cat "/home/user/tutorial/code/easyfranchise/deployment/k8s/$PROJECT.yaml" | sed "s~<image-name>~$BTPSA_KYMA_IMAGE_NAME_BUSINESS_PARTNER_MOCK~g" | kubectl apply -f -

  echo
  log "####################################################################################################"
  log "# Deployment Successful"
  log "####################################################################################################"
  echo

fi
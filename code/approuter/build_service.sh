#!/usr/bin/env bash

#
#
echo "================================================================================================="
echo "This script is only used during development to quickly deploy updates to a DEV cluster. "
echo "It is not called by the Jenkins. You should also not use it to patch or update a live cluster."
echo "================================================================================================="
echo ""

programname=$0
DRY_RUN=false
ENV_FILE="../.env"
function usage {
    echo "usage: $programname [-e=<.env file> | --env=<.env file>] [-d | --dry-run] [-h]"
    echo "  -e=, --env=      ** mandatory **       specify .env file"
    echo "  -d, --dry-run    ** optional **        print rendered yaml file without deploying to Kyma"
    echo "  -h               ** optional **        display help"
    exit 1
}

# parse command line arguments
for i in "$@"; do
  case $i in
    -e=*|--env=*)
      ENV_FILE="${i#*=}"
      shift # past argument=value
      ;;
    -d|--dry-run)
      DRY_RUN=true
      shift # past argument with no value
      ;;
    -h|--help)
      usage
      ;;
    *)
      # unknown option
      echo "Error: Unknown argument ${i}"
      usage
      ;;
  esac
done

# mandatory arguments
if [ ! "$ENV_FILE" ]; then
  echo "Error: .env file must be provided"
  usage
fi

# convert relative path to absolute path
ABS_ENV_FILE="$(cd "$(dirname "$ENV_FILE")"; pwd)/$(basename "$ENV_FILE")"

echo "Info: Using .env file $ABS_ENV_FILE"

# Settings in .env have prio
if [ -f $ABS_ENV_FILE ]; then
  set -o allexport
  source $ABS_ENV_FILE
  set +o allexport
else 
  echo "Error: $ABS_ENV_FILE does not exist, exiting..."
  exit 1
fi

if [ -z "$CLUSTER_DOMAIN" ]; then
  echo "Error: Need variable 'CLUSTER_DOMAIN' either in '.env' file or as environment variable"  
  exit 1
fi

if [ -z "$PROVIDER_SUBDOMAIN" ]; then
  echo "Error: Need variable 'PROVIDER_SUBDOMAIN' either in '.env' file or as environment variable"  
  exit 1
fi

# make sure .env match the correct cluster_domain info
KYMA_CLUSTER_CONFIG="$(kubectl config view)"
if [[ ${KYMA_CLUSTER_CONFIG} != *"$CLUSTER_DOMAIN"* ]];then
    echo -e "Error: Check your configuration, current Kyma cluster does not match \033[1;31m $CLUSTER_DOMAIN \033[0m defined in provided .env file"
    exit 1
fi

echo -e "Info: Using Provider Subdomain: \033[1;31m $PROVIDER_SUBDOMAIN \033[0m"
echo -e "Info: Using docker Cluster Domain: \033[1;31m $CLUSTER_DOMAIN \033[0m"

# causes the shell to exit if any subcommand or pipeline returns a non-zero status.
set -e

# set debug mode
#set -x


if [ "$DRY_RUN" = true ]; then
  echo 'Info: Showing rendered yaml file'
  echo '-----------------------------------'
  cat ./k8s/deployment_service.yaml | sed "s~<provider-subdomain>~$PROVIDER_SUBDOMAIN~g" | sed "s~<cluster-domain>~$CLUSTER_DOMAIN~g" | echo "$(cat -)"  
  echo '-----------------------------------'
else
  # make sure no active subscription exists
  echo "Info: Checking active subscription..."
  ACTIVE_SUBSCRIPTION=$(./checkActiveSubscription.sh | tail -1)

  if [ "$ACTIVE_SUBSCRIPTION" -gt 0 ]; then
    echo "Error: there are $ACTIVE_SUBSCRIPTION active subscription! exiting..."
    exit 1
  else
    echo "Info: No active subscription exists"
  fi
  
  echo 'Info: Deploying services'
  cat ./k8s/deployment_service.yaml | sed "s~<provider-subdomain>~$PROVIDER_SUBDOMAIN~g" | sed "s~<cluster-domain>~$CLUSTER_DOMAIN~g" | kubectl apply -f -
fi

echo 'Info: Done!'

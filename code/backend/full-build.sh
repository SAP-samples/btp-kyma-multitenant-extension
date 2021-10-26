#!/usr/bin/env bash

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

# convert relative path to absolute path, otherwise build.sh in subfolder will break
ABS_ENV_FILE="$(cd "$(dirname "$ENV_FILE")"; pwd)/$(basename "$ENV_FILE")"

echo "Build and Deploy Easy Franchise Backend"
echo
echo "Step 0: Maven build"
echo 
mvn clean install
echo 
echo

echo "Step 1: Deploy Configmap"
echo 
kubectl apply -n backend -f ./config/backend-configmap.yaml
kubectl apply -n integration -f ./config/backend-configmap.yaml
echo 
echo


echo
echo "Step 2: Build and Deploy DB-Service"
echo 
cd db-service
if [ "$DRY_RUN" = true ]; then
    ./build.sh -e=$ABS_ENV_FILE --dry-run
else
    ./build.sh -e=$ABS_ENV_FILE
fi
cd ..
echo 
echo 

echo "Step 3: Build and Deploy BP-Service"
echo 
cd bp-service
if [ "$DRY_RUN" = true ]; then
    ./build.sh -e=$ABS_ENV_FILE --dry-run
else
    ./build.sh -e=$ABS_ENV_FILE
fi
cd ..
echo 
echo 

echo "Step 4: Build and Deploy EF-Service"
echo 
cd ef-service
if [ "$DRY_RUN" = true ]; then
    ./build.sh -e=$ABS_ENV_FILE --dry-run
else
    ./build.sh -e=$ABS_ENV_FILE
fi
cd ..
echo 
echo 

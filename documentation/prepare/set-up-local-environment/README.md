# Set Up the Development Environment

To execute all the necessary steps of the tutorial, you will need the following software available on your machine:

* Java: JDK16 or later. We recommend [OpenJDK](https://openjdk.java.net/install/).
* [Maven](https://maven.apache.org/install.html)
* [Node.js](https://nodejs.org/en/download/)
* [Docker](https://hub.docker.com/)
* [Docker Hub Account](https://docs.docker.com/get-started/#download-and-install-docker)
* [Kubernetes CLI](https://kubernetes.io/docs/tasks/tools/#kubectl)
* [Kubernetes OpenID Connect (OIDC) authentication](https://github.com/int128/kubelogin)
* [jq](https://stedolan.github.io/jq/) 
* [uuidgen](https://packages.ubuntu.com/bionic/uuid-runtime)

## Windows User

We recommend using a Linux subsystem for the tutorial as our scripts are only available as bash scripts. Furthermore, most of the examples around Kubernetes, for example, are written for Linux/MacOs environments. See [Install WSL](https://docs.microsoft.com/en-us/windows/wsl/install) in the Microsoft documentation for more details.

We recommend using Ubuntu 22.04, as older versions do not support OpenJDK11 or later.
If you have chosen to use the Linux, you need to choose the Linux installation option for the following tools.

## Local Development and Testing

### Java - Java Development Kit

You can choose any JDK provider that you prefer. We recommend using [OpenJDK](https://openjdk.java.net/install/). Make sure that you have at least JDK16 installed.

To verify your installation, type the following in a shell:

```shell
java -version
```

The output should look like this:

```shell
openjdk version "17-ea" 2021-09-14
OpenJDK Runtime Environment (build 17-ea+19-Ubuntu-1ubuntu1)
OpenJDK 64-Bit Server VM (build 17-ea+19-Ubuntu-1ubuntu1, mixed mode, sharing)
```

### Maven

You can download the latest version available from [Installing Apache Maven](https://maven.apache.org/install.html).

To verify your installation, type the following in a shell:

```shell
mvn -v
```

The output should look like this:

```shell
Apache Maven 3.6.3
Maven home: /usr/share/maven
Java version: 17-ea, vendor: Private Build, runtime: /usr/lib/jvm/java-17-openjdk-amd64
Default locale: en, platform encoding: UTF-8
OS name: "linux", version: "5.4.72-microsoft-standard-wsl2", arch: "amd64", family: "unix"
```

### Node.js

You can download the latest Node.js version from [Node.js Downloads](https://nodejs.org/en/download/).

To verify your installation, type the following in a shell:

```shell
node -v
```

The output should look like this:

```shell
v12.21.0
```

## Deployment to Kyma

### Kubectl

You can download the Kubernetes command line tool, from [kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl).

To verify your installation, type the following in a shell:

```shell
kubectl version
```

The output should look like this:

```shell
Client Version: version.Info{Major:"1", Minor:"21", GitVersion:"v1.21.3", GitCommit:"ca643a4d1f7bfe34773c74f79527be4afd95bf39", GitTreeState:"clean", BuildDate:"2021-07-15T21:04:39Z", GoVersion:"go1.16.6", Compiler:"gc", Platform:"linux/amd64"}
```

### Kubernetes OpenID Connect (OIDC)

To login to your Kyma Cluster you also need the OpenID Connect plugin for the Kubectl. The plugin can be installed as described [here](https://github.com/int128/kubelogin#getting-started).

### Docker

You can install Docker from [Docker Hub](https://hub.docker.com/).

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

### Docker Hub Account

To be able to deploy the application, you will first need a place to store your images. If you don't have an image reposiotry already available in your company, you can create a [Docker Hub Account](https://docs.docker.com/get-started/#download-and-install-docker) to store the images.


### jq

[jq](https://stedolan.github.io/jq/) is a lightweight and flexible command-line JSON processor for UNIX.

It will be needed by calling manual or script-based deployment.


1. Check if you have **jq** installed:

   ```
   jq --version
   ```

2. If you need to install it, you can do this with the following command:

   ```
   sudo apt-get install jq
   ```

### uuidgen

1. Check if you have **uuidgen** installed:

   ```
   uuidgen
   ```

2. If you need to install it, you can do this with the following command:

   ```
   sudo apt-get install uuid-runtime
   ```


## Get the Project Source

Choose one of the options to get the sources on your local disk:

* [Download](https://github.com/SAP-samples/btp-kyma-multitenant-extension/archive/refs/heads/main.zip) the ZIP file and extract the source files.

* Use the [git clone](https://git-scm.com/) command:

  ```
  git clone https://github.com/SAP-samples/btp-kyma-multitenant-extension.git
  ```

* Clone the repository using the GitHub CLI:

  ```
  gh repo clone SAP-samples/btp-kyma-multitenant-extension
  ```


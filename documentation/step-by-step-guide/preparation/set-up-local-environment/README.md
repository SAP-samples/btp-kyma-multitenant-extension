# Set up the development environment

In order to execute all the neccessary steps of the tutorial you will need the follwing software available on your PC/Laptop. 

Overview:
* Java (min. JDK16) (we recommend [OpenJDK](https://openjdk.java.net/install/))
* [maven](https://maven.apache.org/install.html)
* [Node.js](https://nodejs.org/en/download/)
* [Docker](https://hub.docker.com/)
* [Docker Hub Account](https://docs.docker.com/get-started/#download-and-install-docker) 
* [Kubernetes CLI](https://kubernetes.io/docs/tasks/tools/#kubectl)

## Windows User

We recommend to use a linux subsystem for the tutorial as our scripts are only available as bash scripts and furthermore most of the examples around kubernetes etc. are written for Linux/Mac environments. An installation description can be found [here](https://docs.microsoft.com/en-us/windows/wsl/install). We recommend to use Ubuntu 21.04 which is available as community preview, as older versions do not support OpenJDK >= 11. The preview version is available [here](https://www.microsoft.com/store/apps/9P9Q5ZH1HRR0). 

If you have choosen to use the linux you need to choose the linux installation option for the tools below.

## Local Development and Testing

### Java - Java Development Kit

Basically you can choose any JDK provider that you prefer. We recommend the use of [OpenJDK](https://openjdk.java.net/install/). Make sure that you have at least JDK16 installed.

To verify your installation type the following in a shell:
```shell
java -version
```

The output should look similar to this: 
```shell
openjdk version "17-ea" 2021-09-14
OpenJDK Runtime Environment (build 17-ea+19-Ubuntu-1ubuntu1)
OpenJDK 64-Bit Server VM (build 17-ea+19-Ubuntu-1ubuntu1, mixed mode, sharing)
```

### Maven
You can use the latest version available [here](https://maven.apache.org/install.html)

To verify your installation type the following in a shell:
```shell
mvn -v
```

The output should look similar to this: 
```shell
Apache Maven 3.6.3
Maven home: /usr/share/maven
Java version: 17-ea, vendor: Private Build, runtime: /usr/lib/jvm/java-17-openjdk-amd64
Default locale: en, platform encoding: UTF-8
OS name: "linux", version: "5.4.72-microsoft-standard-wsl2", arch: "amd64", family: "unix"
```

### Node.js
The latest Nodejs version can be downloaded from [here](https://nodejs.org/en/download/). 

To verify your installation type the following in a shell:
```shell
node -v
```

The output should look similar to this: 
```shell
v12.21.0
```

## Deployment to Kyma

### Kubernetes CLI
The Kubernetes command line tool can be found [here](https://kubernetes.io/docs/tasks/tools/#kubectl). 

To verify your installation type the following in a shell:
```shell
kubectl version
```

The output should look similar to this: 
```shell
Client Version: version.Info{Major:"1", Minor:"21", GitVersion:"v1.21.3", GitCommit:"ca643a4d1f7bfe34773c74f79527be4afd95bf39", GitTreeState:"clean", BuildDate:"2021-07-15T21:04:39Z", GoVersion:"go1.16.6", Compiler:"gc", Platform:"linux/amd64"}
```

### Docker
Docker can be installed from [here](https://hub.docker.com/)

To verify your installation type the following in a shell:
```shell
docker version
```

The output should look similar to this: 
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
For deployment you will need a place to store your images. If you don't have an image repositry already available in your company you can create yourself a [Docker Hub Account](https://docs.docker.com/get-started/#download-and-install-docker) to store the images.

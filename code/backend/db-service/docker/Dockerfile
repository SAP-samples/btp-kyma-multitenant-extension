FROM sapmachine:16
LABEL maintainer="alexander.rieder@sap.com"

WORKDIR /var/app

ADD target/db-service.jar /var/app/db-service.jar
ADD target/dependency/* /var/app/

ENV JAVA_OPTS=""

EXPOSE 8080

CMD ["java", "-cp",  "/var/app/*", "dev.kyma.samples.easyfranchise.ServerApp", "8080"]
#CMD java $JAVA_OPTS -cp /var/app/* dev.kyma.samples.javaserver.ServerApp 8080
#CMD [ "/bin/ls", "-l" ]     
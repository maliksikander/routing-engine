FROM adoptopenjdk:16-jdk-openj9
MAINTAINER CTI Team Expertflow
WORKDIR /app
#RUN apk add curl=7.78.0-r0
#RUN apk update && apk add tzdata
Run apt-get -y install tzdata
COPY media-routing-engine.jar media-routing-engine.jar
COPY docker-entrypoint.sh docker-entrypoint.sh
RUN echo "user:x:1001060000:0::/app:" >> /etc/passwd \
&& echo "user:!:$(($(date +%s) / 60 / 60 / 24)):0:99999:7:::" >> /etc/shadow \
&& echo "user:x:1001060000:" >> /etc/group \
&& chmod -R 775 /app /var/log \
&& chown -R user:root /app;
RUN chmod a+x docker-entrypoint.sh
USER 1001060000
ENTRYPOINT ["./docker-entrypoint.sh"]
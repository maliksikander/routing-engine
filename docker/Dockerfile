FROM gitlab.expertflow.com:9242/general/openjdk:8-jre-alpine-3.11
MAINTAINER CTI Team Expertflow
WORKDIR /app
RUN apk update && apk add tzdata
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
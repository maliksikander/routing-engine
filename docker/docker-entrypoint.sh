#!/bin/sh
set -e

exec java -Xshareclasses -Xquickstart \
  -Dfile.encoding=utf-8 -jar -Dspring.profiles.active=prod media-routing-engine.jar "$@"

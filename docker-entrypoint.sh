#!/bin/sh
set -e

exec java -Dfile.encoding=utf-8 -jar  media-routing-engine.jar $@

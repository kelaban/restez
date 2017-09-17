#!/usr/bin/env bash

DIR=$(dirname "${BASH_SOURCE}")
. $DIR/lib/init.sh

if should_deploy; then
    mvn deploy -P sign,build-extras --settings $DIR/mvnsettings.xml
fi

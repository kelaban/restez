#!/usr/bin/env bash

DIR=$(dirname "${BASH_SOURCE}")
. $DIR/lib/init.sh

if should_deploy; then
    openssl aes-256-cbc -K $encrypted_9eca9097811c_key -iv $encrypted_9eca9097811c_iv -in $DIR/codesigning.asc.enc -out $DIR/codesigning.asc -d
    gpg --fast-import $DIR/signingkey.asc
fi

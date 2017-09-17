#!/usr/bin/env bash

set -e

# This script will verify that this develop branch can be release
# By doing the the following:
# - Is this the develop branch?
# - Is this a SNAPSHOT version
# - Are there changes to be commited?
# - Did the last test for this repo on develop pass on travis-ci?
# - Was the last test against the same SHA?

DIR=$(dirname "${BASH_SOURCE}")
. $DIR/lib/travis-api.sh
. $DIR/lib/maven-utils.sh

REPO=kelaban/restez


branch=$(git rev-parse --abbrev-ref HEAD)

if [ $branch != "develop" ]; then
  echo "MUST be run from develop branch"
  exit 1;
fi

version=$(maven::evaluate project.version)

if ! [[ $version =~ -SNAPSHOT$ ]]; then
  echo 'project.version must end in "-SNAPSHOT" check pom.xml'
  exit 1
fi

if [ -n "$(git status --porcelain)" ]; then
  echo "there are changes on the current branch, make sure everything is checked in or stashed";
  exit 1
fi

resp=$(travis::get "repos/$REPO/branches/develop")
build_sha=$(echo $resp | jq -r '.commit.sha')
build_num=$(echo $resp | jq -r '.branch.number')
build_state=$(echo $resp | jq -r '.branch.state')
current_sha=$(git rev-parse --verify HEAD)

if [ $build_sha = $current_sha ]; then
  if [ $build_state != "passed" ]; then
    echo "The last build is not passed! was: $build_state"
    exit 1;
  fi
  echo "This branch is good to release!"
  exit 0
else
  echo "last built SHA: '$build_sha' for build #$build_num does not match current HEAD: '$current_sha'"
  exit 1
fi

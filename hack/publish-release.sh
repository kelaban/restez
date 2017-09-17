#!/usr/bin/env bash

set -e

# This script will merge the develop branch onto the master branch
# and publish it. This will trigger a travis-ci build and deploy
#
# - Verify relase
# - Checkout master
# - Merge develop into master
# - Remove  "-SNAPSHOT" from version
# - push master and tag release version
# - merge back into develop branch
# - add increment version and add -SNAPSHOT

DIR=$(dirname "${BASH_SOURCE}")
. $DIR/lib/travis-api.sh
. $DIR/lib/maven-utils.sh

# set -e will cause this to abort if verify fails
$DIR/verify-release.sh

# Just incase set -e doesn't do it's job
if [ $? -ne 0 ]; then
  echo "Aboriting release, make sure verify-release.sh passes!"
  exit 1;
fi


git checkout master
git merge develop

cur_snapshot=$(maven::evaluate project.version)
release_version=$(echo $cur_snapshot | sed -e 's/-SNAPSHOT//')

echo "Updating to next version $release_version"

mvn --batch-mode versions:set -DnewVersion=$release_version

git add pom.xml
git commit -a -m "[RELEASE] - Preparing release $release_version"
git tag $release_version
git push origin HEAD
git push origin --tags HEAD

git checkout develop
git merge master

mvn --batch-mode versions:set -DnextSnapshot=true
git add pom.xml
git commit -a -m "[RELEASE] - Update to next SNAPSHOT"
git push origin HEAD

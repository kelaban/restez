
function should_deploy() {
  if [ "$TRAVIS_BRANCH" = 'master' ] || \
     [ "$TRAVIS_BRANCH" = 'develop' ] && \
     [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    return 0
  else
    return 1
  fi
}

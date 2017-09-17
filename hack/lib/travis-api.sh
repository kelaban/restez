TRAVIS_API_ENDPONT=https://api.travis-ci.org
TRAVIS_API_TOKEN=$(travis token --no-interactive)

travis::get() {
  uri=$TRAVIS_API_ENDPONT/$1

  curl -s -X GET \
     -H "User-Agent: k317h" \
     -H "Accept: application/vnd.travis-ci.2+json" \
     -H "Authorization: token $TRAVIS_API_TOKEN" \
     $uri
}

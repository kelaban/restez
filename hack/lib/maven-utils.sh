maven::evaluate() {
  expression=$1
  mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=$expression 2>/dev/null | grep -v '^\['
}

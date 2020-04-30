#!/bin/bash -e

if [[ $GIT_BRANCH != master ]]; then
  echo "we only run sonar scanner on master"
  exit 0
fi

version="$(grep -m1 version build.gradle  | awk -F\' '{print $2}')"

if [[ -z "$version" ]]; then
  echo >&2 "unable to get version number!"
  exit 1
fi

if [[ -z "$SONAR_TOKEN" ]]; then
  echo >&2 "not scanning because missing sonar token"
  exit 1
fi

sonar-scanner \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.organization=allcleardev \
  -Dsonar.projectKey=allcleardev \
  -Dsonar.projectName=allclear-service \
  -Dsonar.projectVersion="$version" \
  -Dsonar.java.binaries='build/classes'

exit 0

#!/bin/sh
  arg=${1:-10}
  ./gradlew :Server:run --console=plain -q --args=$arg

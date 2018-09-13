#!/usr/bin/env bash

./gradlew --stacktrace lintDebug checkstyleMain pmdMain pmdTest

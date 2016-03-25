#!/usr/bin/env bash
echo "Downloading certs for key $1"
echo "Downloading certs for test $2"
echo "ENV Key $ANYSOFTKEYBOARD_KEYSTORE_ALIAS"
echo "ENV Test $MY_TEST_ENV_VAL"

wget $KEYSTORE_FILE_URL -q -O /tmp/anysoftkeyboard.keystore
wget $PUBLISH_CERT_FILE_URL -q -O /tmp/apk_upload_key.p12

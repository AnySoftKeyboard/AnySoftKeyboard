#!/usr/bin/env bash

wget $KEYSTORE_FILE_URL -q -O /tmp/anysoftkeyboard.keystore
wget $PUBLISH_CERT_FILE_URL -q -O /tmp/apk_upload_key.p12

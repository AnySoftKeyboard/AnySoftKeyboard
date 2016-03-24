#!/usr/bin/env bash
wget ${KEYSTORE_FILE_URL} -q -O stalker.keystore
wget ${PUBLISH_CERT_FILE_URL} -q -O apk_upload_key.p12

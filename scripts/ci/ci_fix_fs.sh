#!/usr/bin/env bash
chmod -R a+rwx .
find . -type d -exec chmod 755 {} \;
find . -type f -name "*.gradle" -exec chmod 644 {} \;

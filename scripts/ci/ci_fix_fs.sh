#!/usr/bin/env bash
find . -type d -exec chmod 755 {} \;
find . -type f -name "*.gradle" -exec chmod 644 {} \;

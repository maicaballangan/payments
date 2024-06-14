#!/bin/sh
# API Build Script

echo "Removing existing dirs..."
rm -rf modules lib precompiled javadoc test-result jacoco.exec
echo "Getting dependencies..."
play clean
play deps --sync --forProd
rm -rf tmp modules/i18ntools-1.0.1/.git

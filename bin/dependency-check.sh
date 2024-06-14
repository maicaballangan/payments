#!/bin/sh

echo "Running dependency checker..."

./bin/dependency-check/bin/dependency-check.sh --disableAssembly --project "academy-api" --scan "lib" --out "."

echo "Opening browser to report..."
if [ "$OSTYPE" == "linux-gnu" ]
then
xdg-open dependency-check-report.html
else
open dependency-check-report.html
fi

#!/bin/sh

if [ ! $# -ge 1 ]
then
        PLAY_ENV="test"
fi
echo "Running code coverage..."
echo "Executing tests..."
rm -rf test-result
play clean
play precompile
play autotest --%$PLAY_ENV

java -jar bin/jacococli.jar report jacoco.exec --classfiles precompiled/java --html test-result/code-coverage --name api --sourcefiles app --xml jacoco.xml

echo "Opening browser to report..."
if [ "$OSTYPE" == "linux-gnu" ]
then
xdg-open test-result/code-coverage/index.html
else
open test-result/code-coverage/index.html
fi

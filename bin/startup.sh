#!/bin/bash

if type -p java; then
    echo found java executable in PATH
    _java=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    echo found java executable in JAVA_HOME     
    _java="$JAVA_HOME/bin/java"
else
    echo "no java"
fi

if [[ "$_java" ]]; then
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo version "$version"
    if [[ "$version" > "11" ]]; then
        echo version is more than 1.8
        _commercial="-XX:+UnlockExperimentalVMOptions -XX:+UseZGC"
    else         
        echo version is 8
        _commercial="-XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:+UseG1GC"
    fi
fi

if [ "$1" == "clear" ] || [ "$2" == "clear" ] || [ "$3" == "clear" ] || [ "$4" == "clear" ] || [ "$5" == "clear" ] || [ "$6" == "clear" ]
then
echo "Initializing project and downloading dependencies...This may take some time..."
rm -rf lib modules precompiled
play clean
play deps --sync
rm -rf modules/i18ntools-1.0.1/.git
play precompile
else
echo "Skipping clearing directories"
fi

if [ "$1" == "ide" ] || [ "$2" == "ide" ] || [ "$3" == "ide" ] || [ "$4" == "ide" ] || [ "$5" == "ide" ] || [ "$6" == "ide" ]
then
echo "Creating IDE project files..."
play idea
play eclipsify
play netbeansify
else
echo "Skipping IDE project file creation..."
fi

if [ "$1" == "test" ] || [ "$2" == "test" ] || [ "$3" == "test" ] || [ "$4" == "test" ] || [ "$5" == "test" ] || [ "$6" == "test" ]
then
echo "Running unit/functional tests..."
play autotest
else
echo "Skipping tests..."
fi

if [ "$1" == "start" ] || [ "$2" == "start" ] || [ "$3" == "start" ] || [ "$4" == "start" ] || [ "$5" == "start" ] || [ "$6" == "start" ]
then
echo "Opening browser to API..."
play stop
test -f server.pid && kill -15 $(cat "server.pid")
echo "Stopping existing play server..."
sleep 5
rm -rf server*.pid
play clean
play start -Xms512M -Xmx768M -XX:+UnlockDiagnosticVMOptions $_commercial -XX:+UseAES -XX:+UseAESIntrinsics -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -XX:OnOutOfMemoryError="gcore %p" -XX:MaxMetaspaceSize=1g -XX:+AlwaysPreTouch
echo "Starting play server..."
sleep 5
if [ "$OSTYPE" == "linux-gnu" ] 
then
xdg-open http://localhost:9009/v1
else
open http://localhost:9009/v1
fi
else
echo "Skipping start..."
fi

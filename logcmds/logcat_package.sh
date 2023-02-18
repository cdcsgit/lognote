#! /bin/sh

trap 'kill -9 $(jobs -pr)' EXIT
trap 'exit' SIGINT SIGTERM
PACKAGE= # PACKAGE_NAME
ADB= # ADB_PATH
DEVICE= # DEVICE_NAME
PID=0
PID_NEW=0
PID_ADB=0
while [ true ]
do
    PID_NEW=`$ADB -s $DEVICE shell pidof $PACKAGE`

    if [ "a$PID" != "a$PID_NEW" ]; then
        echo LogNote - restart logcat
        PID=$PID_NEW
        if [ "a$PID_ADB" != "a0" ]; then
            kill -9 $PID_ADB
        fi
        $ADB -s $DEVICE logcat --pid=$PID &
        PID_ADB=$!
    fi
    sleep 3

done

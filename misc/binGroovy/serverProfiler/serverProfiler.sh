#!/bin/bash

yearMonthFolder=`/bin/date "+%Y/%m"`
day=`/bin/date "+%d"`

dayTime=`/bin/date "+%Y_%m_%d__%H_%M_%S_%3N"`

logFilePrefix=/tmp/fastServerProfiler/"$yearMonthFolder"

mkdir -p "$logFilePrefix"
logFileFile="$logFilePrefix"/fastServerProfiler_"$day".log

echo "#####   $dayTime  #####" >> $logFileFile

/opt/appserv/common/groovy/bin/groovy /opt/appserv/common/binGroovy/serverProfiler.groovy >> $logFileFile 2>&1

echo >> $logFileFile
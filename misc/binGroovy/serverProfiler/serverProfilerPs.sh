#!/bin/bash

if [ $# -ne "1" ]; then
  echo
  echo "ERROR: Give the pid of the process"
  echo
  exit 1
fi


pid=$1

/usr/bin/sudo /bin/ps -ef | grep "$pid" | grep -v grep | grep -v serverProfilerPs.sh
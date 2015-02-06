#!/bin/bash

for i in 6 7
do

for w in 1024 2048 4096 8192 16384 32768
do
  echo "integral 1 $i gpu workers $w"
  WORKERS=$w FORCE=GPU $APPLEBIN/bin/_repeat.sh sudo -E bash utils/run_integral.sh 1 $i | grep -v R
done

done
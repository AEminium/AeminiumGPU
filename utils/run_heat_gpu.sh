#!/bin/bash
for i in 100 500 1000 5000 10000 50000 100000 500000 1000000
do
  echo "heat $i 100 gpu"
  WORKERS=8192 FORCE=GPU $APPLEBIN/bin/_repeat.sh sudo -E bash utils/run_heat.sh 1 $i | grep -v R
done

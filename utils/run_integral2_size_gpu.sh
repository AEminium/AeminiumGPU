#!/bin/bash
for i in {1..7}
do
  echo "integral 1 $i gpu"
  FORCE=GPU $APPLEBIN/bin/_repeat.sh sudo -E bash utils/run_integral2.sh 1 $i | grep -v R
done

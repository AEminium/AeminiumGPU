#!/bin/bash
for i in {1..24..2}
do
  echo "integral $i 2 gpu"
  WORKERS=512 FORCE=GPU $APPLEBIN/bin/_repeat.sh sudo -E bash utils/run_integral.sh $i 2 | grep -v R
done
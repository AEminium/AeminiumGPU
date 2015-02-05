#!/bin/bash
for i in 1 {2..28..2}
do
  echo "integral $i 2 cpu"
  WORKERS=512 FORCE=CPU $APPLEBIN/bin/_repeat.sh sudo -E bash utils/run_integral.sh $i 2 | grep -v R
done
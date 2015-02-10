#!/bin/bash
for i in {1..7}
do
  echo "integral 1 $i cpu"
  FORCE=CPU $APPLEBIN/bin/_repeat.sh sudo -E bash utils/run_integral2.sh 1 $i | grep -v R
  sudo /sbin/sysctl -w vm.drop_caches=1 > /dev/null
  sudo /sbin/sysctl -w vm.drop_caches=2 > /dev/null
  sudo /sbin/sysctl -w vm.drop_caches=3 > /dev/null
  sleep 1
done

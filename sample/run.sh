#!/bin/bash
# declare an array called array and define 3 vales

node=( 3 5 7 9 11 13 15)
per=( 0.3 )
fail=( 'true')
for n in "${node[@]}"
  do
  for p in "${per[@]}"
  do
    for f in "${fail[@]}"
    do
      java -cp 'target/wpaxos.sample-1.0.0.jar:target/dependency/*' com.wuba.wpaxos.sample.latency.LatencyTestMain $n $p 20000 $f
    done
  done 
done
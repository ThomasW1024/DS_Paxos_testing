#!/usr/bin/env bash
echo "Start the testing plan 30% proposers 0 failure 64bytes"
# Argument number of node, percentage of proposers, percentage of failure nodes, thread waiting time, message length in byte, process time out
for w in 100 500
  do
    java -jar sample/target/paxos_runtime_testcase_runner-jar-with-dependencies.jar 3 30 0 $w 64 60
    echo "----------- Waiting ------------"
    sleep 5
    java -jar sample/target/paxos_runtime_testcase_runner-jar-with-dependencies.jar 5 30 0 $w 64 60
    echo "----------- Waiting ------------"
    sleep 5
    java -jar sample/target/paxos_runtime_testcase_runner-jar-with-dependencies.jar 7 30 0 $w 64 60
    echo "----------- Waiting ------------"
    sleep 5
    java -jar sample/target/paxos_runtime_testcase_runner-jar-with-dependencies.jar 9 30 0 $w 64 60
    echo "----------- Waiting ------------"
    sleep 5
    java -jar sample/target/paxos_runtime_testcase_runner-jar-with-dependencies.jar 11 30 0 $w 64 180
    echo "----------- Waiting ------------"
    sleep 15
    java -jar sample/target/paxos_runtime_testcase_runner-jar-with-dependencies.jar 13 30 0 $w 64 240
    echo "----------- Waiting ------------"
    sleep 15
    java -jar sample/target/paxos_runtime_testcase_runner-jar-with-dependencies.jar 15 30 0 $w 64 300
    echo "----------- Waiting ------------"
    sleep 15
    echo "----------- Test End ------------"
done
#!/usr/bin/env bash
echo "Start the testing plan"
java -jar sample/target/paxos_runtime_testcase_runner-jar-with-dependencies.jar 3 30 20 500 64 60
echo "----------- Waiting ------------"
sleep 5
java -jar sample/target/paxos_runtime_testcase_runner-jar-with-dependencies.jar 5 30 20 500 64 60
echo "----------- Waiting ------------"
sleep 5
java -jar sample/target/paxos_runtime_testcase_runner-jar-with-dependencies.jar 7 30 20 500 64 60
echo "----------- Waiting ------------"
sleep 5
java -jar sample/target/paxos_runtime_testcase_runner-jar-with-dependencies.jar 9 30 20 500 64 60
echo "----------- Waiting ------------"
sleep 5
java -jar sample/target/paxos_runtime_testcase_runner-jar-with-dependencies.jar 11 30 20 500 64 180
echo "----------- Waiting ------------"
sleep 15
java -jar sample/target/paxos_runtime_testcase_runner-jar-with-dependencies.jar 13 30 20 500 64 240
echo "----------- Waiting ------------"
sleep 15
java -jar sample/target/paxos_runtime_testcase_runner-jar-with-dependencies.jar 15 30 20 500 64 300
echo "----------- Waiting ------------"
sleep 15
echo "----------- Test End ------------"
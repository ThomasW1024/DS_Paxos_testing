# WPaxos Downaload:

git clone https://github.com/wuba/WPaxos.git

# importing test case:
  Download from : https://version-control.adelaide.edu.au/svn/a1797827/2020/s2/ds/assignment4
  copy all the file inside the SVN repository into the root of the project.
  and append /sample with the /sample folder

# Building project:
  run maven build
  run maven install
  run maven package on the root of the project

# Running RunTime test case:
  ./test_plan_1.sh and ./test_plan_2.sh

# Running Latency test case:
  ./sample/latency_test_run.sh

# files added.
'+ sample/conf/*
'+ sample/db
'+ sample/LatencyDb
'+ sample/logs/*
'M sample/pom.xml
'+ sample/latency_test_run.sh
'+ sample/Dockerfile
'+ sample/docker-compose.yml
'+ sample/src/main/resoucres/*
'+ sample/src/main/java/com/wpaxos/sample/testcase/*
'+ sample/src/main/java/com/wpaxos/sample/runtime/*
'+ sample/src/main/java/com/wpaxos/sample/latency/*
'+ README_TEST.md
'+ assg4-testcases_runtime.xlsx
'+ assg4-latency_result.xlsx
'+ test_plan_2.sh
'+ test_plan_1.sh
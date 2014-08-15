This script will print out a summary of performance statistics for the server.
It will rollup the java and apache processes to give a summary.
The processes which are using a lot or memory or cpu will be called out at the top.

Put this in a cron every minute and it will print out a report in a file for each day (/tmp assumed to have old files purged).

* * * * * /opt/appserv/common/binGroovy/serverProfiler.sh

See the output:

[appadmin@fasttest-mgmt-01 ~]$ tail -40 /tmp/fastServerProfiler/2014/08/fastServerProfiler_15.log 
#####   2014_08_15__11_01_01_911  #####
Process 15805: pennInTouch, 491m, 120.8% cpu, 8.4% mem
Overall CPU: 50.0%, memUsed: 4.16g / 5.71g (72.9%), Load (1,5,15min) 3.52, 1.99, 0.80
httpd (4 processes, 256m, 0% cpu, 4.4% mem)
java (24 processes, 2.96g, 120.8% cpu, 52.3% mem)

#####   2014_08_15__11_02_01_708  #####
Process 15805: pennInTouch, 513m, 66.9% cpu, 8.8% mem
Overall CPU: 66.7%, memUsed: 4.22g / 5.71g (73.91%), Load (1,5,15min) 3.65, 2.43, 1.03
httpd (5 processes, 326m, 0% cpu, 5.6% mem)
java (24 processes, 2.99g, 66.9% cpu, 52.7% mem)

#####   2014_08_15__11_03_01_556  #####
Process 15805: pennInTouch, 566m, 66.4% cpu, 9.7% mem
Overall CPU: 33.3%, memUsed: 4.3g / 5.71g (75.28%), Load (1,5,15min) 1.63, 2.06, 0.99
httpd (6 processes, 393m, 0% cpu, 6.8% mem)
java (24 processes, 3.04g, 66.4% cpu, 53.6% mem)

#####   2014_08_15__11_04_01_350  #####
Process 15805: pennInTouch, 564m, 140.2% cpu, 9.6% mem
Process 31978: httpd.worker, 65m, 46.7% cpu, 1.1% mem
Overall CPU: 100.0%, memUsed: 4.3g / 5.71g (75.24%), Load (1,5,15min) 1.18, 1.84, 0.98
httpd (6 processes, 392m, 46.7% cpu, 6.8% mem)
java (24 processes, 3.04g, 140.2% cpu, 53.5% mem)

#####   2014_08_15__11_05_01_262  #####
Process 15805: pennInTouch, 575m, 321.8% cpu, 9.8% mem
Process 32392: top, 1.24k, 53.6% cpu, 0% mem
Overall CPU: 100.0%, memUsed: 4.31g / 5.71g (75.51%), Load (1,5,15min) 1.98, 1.89, 1.05
httpd (6 processes, 392m, 0% cpu, 6.8% mem)
java (24 processes, 3.05g, 321.8% cpu, 53.7% mem)

#####   2014_08_15__11_06_01_699  #####
Process 15805: pennInTouch, 574m, 0% cpu, 9.8% mem
Overall CPU: 25.0%, memUsed: 4.32g / 5.71g (75.56%), Load (1,5,15min) 1.61, 1.79, 1.06
httpd (6 processes, 393m, 0% cpu, 6.8% mem)
java (24 processes, 3.04g, 0% cpu, 53.7% mem)

#####   2014_08_15__11_07_01_321  #####
Process 15805: pennInTouch, 564m, 0% cpu, 9.7% mem
Overall CPU: 25.0%, memUsed: 4.31g / 5.71g (75.51%), Load (1,5,15min) 0.76, 1.51, 1.01
httpd (7 processes, 440m, 0% cpu, 7.5% mem)
java (24 processes, 3.04g, 0% cpu, 53.6% mem)

#####   2014_08_15__11_08_01_552  #####
Process 15805: pennInTouch, 570m, 0% cpu, 9.8% mem
Overall CPU: 40.0%, memUsed: 4.22g / 5.71g (73.87%), Load (1,5,15min) 0.36, 1.25, 0.95
httpd (3 processes, 175m, 0% cpu, 3% mem)
java (24 processes, 3.04g, 0% cpu, 53.7% mem)

#####   2014_08_15__11_09_01_056  #####
Process 15805: pennInTouch, 563m, 0% cpu, 9.6% mem
Overall CPU: 33.3%, memUsed: 4.19g / 5.71g (73.35%), Load (1,5,15min) 0.13, 1.02, 0.89
httpd (2 processes, 110m, 0% cpu, 1.9% mem)
java (24 processes, 3.04g, 0% cpu, 53.5% mem)


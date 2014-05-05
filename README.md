CSCI-6969 Programming Languages, Project 3.2
============================================

***Charles Hathaway, Kathleen Tully***


Bonus Problem 1.
----------------
Test system specifications:

* 6 logical CPU's
* 16G RAM

Method of testing:

Record the system nano time before adding workers to the Executor, run the program, record the end time.
Determine elapsed time by subtracting the end time from the start time, convert to milliseconds so the time isn't ridiculously long winded.

newFixedThreadPool(26 threads) (100 delay): 17309.324749 ms execution time

newFixedThreadPool(10 threads) (100 delay): 11649.038517 ms execution time

newFixedThreadPool(6 threads) (100 delay): 13072.902111 ms execution time

newFixedThreadPool(1 threads) (100 delay): 31341.663880 ms execution time

newCachedThreadPool(0 threads) (100 delay): 12471.750861 ms execution time

newFixedThreadPool(26 threads) (50 delay): 6410.127299 ms execution time

newFixedThreadPool(10 threads) (50 delay): 6152.812549 ms execution time

newFixedThreadPool(6 threads) (50 delay): 6844.178842 ms execution time

newFixedThreadPool(1 threads) (50 delay): 15701.377190 ms execution time

newCachedThreadPool(0 threads) (50 delay): 7099.537122 ms execution time

newFixedThreadPool(26 threads) (200 delay): 26482.821208 ms execution time

newFixedThreadPool(10 threads) (200 delay): 22480.359965 ms execution time

newFixedThreadPool(6 threads) (200 delay): 27047.839905 ms execution time

newFixedThreadPool(1 threads) (200 delay): 62522.340631 ms execution time

newCachedThreadPool(0 threads) (200 delay): 29469.711109 ms execution time

### Analysis

Based on these results, I can deduce the following:

* The delay has the greatest impact on the execution time (doubling it doubles the time, halving it halves the time)

* In general, more threads means better time. This is because we spend most of time sleeping, so more threads do not really burden the CPU, or things might be different

The number of conflicts has a great impact on how this timing works.
With the increment data set, which doesn't conflict at all, adding threads dramatically decreases running time.
Performance will increase up until we reach # threads = # transactions.

With lots of conflicts, however, adding threads only increases performance to a point. 
At some point, there will be too many threads conflicting they will keep aborting without successfully completing.
To support this, observe that 26 threads has a slower time than 10 threads.  


Bonus Problem 2.
-----------------

If we can keep track of transactions that caused an abort in the past, we could check to see if any are present before we start trying to lock resources.
If they are present, we wait until they abort (I would suggest going with the  "optimistic" strategy if one isn't present, otherwise we will need to track "queued transactions").

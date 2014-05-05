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

Note that a thread count of "0" occurs in the CachedThreadPool.
This just indicates that all threads have been closed.

### Results

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
I could see this causing starvation in the real world; thread A can't work with B or C, so it sees B and aborts, then sees C and aborts, then sees B and aborts...
This would not be a very good technique. 

Another thing we could consider is having larger transactions have a delay before they try for resources again.
In the real world, this would result in a starvation issue (if enough small jobs kept coming in, the big ones would never get to play), but for this project it might help.
Of course, with this project, all the transactions in each data set are the same size, so...
Not very helpful.

Delaying after an abort isn't a bad idea; that way we don't lock resources and cause another thread to abort when we will abort in just a few seconds.
A good way to deal with this might be to guess how much time the thread that caused us to abort needs, then schedule ourselves after that thread ends.
For this program, we could calculate the delay as follows: (num_read_variables*delay)+(num_pointers*delay)+(num_write_variables*delay).
So for A = A + 1, we would expect 2*delay (one read, one write).
For A = B + C, we would expect 3*delay (2 reads, one write).

Once again, if we had an infinite data set, the strategy outlined above might not end too well.
We could end up competing with another thread for the same reason, lose, do it again, etc.
But it's not as likely to end up in an infinite loop.
Let's try that one :).

### Results

newFixedThreadPool(26 threads) (100 delay): 12961.524768 ms execution time

newFixedThreadPool(10 threads) (100 delay): 12144.648458 ms execution time

newFixedThreadPool(6 threads) (100 delay): 12872.711836 ms execution time

newFixedThreadPool(1 threads) (100 delay): 31344.533899 ms execution time

newCachedThreadPool(0 threads) (100 delay): 11577.408191 ms execution time

newFixedThreadPool(26 threads) (50 delay): 6264.388558 ms execution time

newFixedThreadPool(10 threads) (50 delay): 6104.389995 ms execution time

newFixedThreadPool(6 threads) (50 delay): 7154.354838 ms execution time

newFixedThreadPool(1 threads) (50 delay): 15714.799433 ms execution time

newCachedThreadPool(0 threads) (50 delay): 7686.142470 ms execution time

newFixedThreadPool(26 threads) (200 delay): 25258.627541 ms execution time

newFixedThreadPool(10 threads) (200 delay): 23655.784603 ms execution time

newFixedThreadPool(6 threads) (200 delay): 26874.130794 ms execution time

newFixedThreadPool(1 threads) (200 delay): 62543.026766 ms execution time

newCachedThreadPool(0 threads) (200 delay): 26857.063254 ms execution time

### Analysis

The best way to describe the results of this test is that it "normalized" all the results.
It brought down the outliers (except for the single thread, but he's special), just made it so everything took about the same amount of time.
It reduced the problem caused by too many threads because instead of arguing, they just waited until the ones using the resources were done.
However, it did slightly increase the time required by (10 threads, 100 delay).
This could be attributed to external factors (such as me starting Pandora), and the variation is so slight that I don't consider it significant.


# StreamDemo
Benchmark for aggregating Streams into `List`s.

This code and the data complement the blog post **Right Sizing Stream Collectors**.

##  How to build and run

The benchmark program that executes the tests is named `app`,
and can be run most simply as a gradle processes.

The simplest start is

```bash
./gradlew run
```

After a few seconds, a short summary of test results will appear on standard out.
After a header that describes the run, the remainder of the output are the results
of each trial.

The output is structured as comma-separated values, with the following columns
tests=kind, elapsed-nanotime, start-mem, start-free, stop-mem, stop-fee.

The benchmark app does have a simple command line interface using positional arguements.
Any omitted argument default to the value enclosed in square braces

- test kind - choice of testA, testB, testAB, testBA [testAb]
- reps per trial - integer [1000]
- size of source collection - integer [100]
- number of trials - integer [10]
- minimum word size - integer [8]
- maximum word size - integer [15]

These values can be pasted into the gradle launch with the `--args` parameter.
For example, these parameters were used to gather data for the blog post.

```bash
./gradlew run --args="testA 10000 42 30"
```

## Gathering Data

For larger scale repetition, it can be better to launch the benchmark app directly
as an installed distribtion.

```bash
./gradlew installDist
```

This creates an installed Java application named `app` at `app/build/install/app/bin/app`.

From the top of the repository tree, this can run from the command line
with the same parameters as the gradle run

```bash
app/build/install/app/bin/app testA 10000 42 30
```

The script file `testCapture.sh` allows ready execution of the benchmark test
for standard size streams.  The results for sequence of tests are written into the
directory specified as a parameter.

```bash
testCapture.sh testA capture_A_now
```

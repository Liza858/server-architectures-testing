# Server architectures testing application

This application allows you to test different server architectures for different parameters such as clients count, task
size, time between requests.

## Description

The essence of the interaction between clients and the server 
in this application is that the clients ask the server to sort an array 
of size `N` (by bubble sort) and receive a sorted array in response.

`M` clients start and send `X` requests with an interval of `delta` milliseconds between 
receiving a response from the server and a new request,
wait for the results and close the connection.

Three types of servers are implemented to process requests from clients:

1. `BLOCKING` - create two threads for each client: for reading and for writing,
reading and writing occur in blocking mode
2. `NON_BLOCKING` - create only two threads for reading and writing,
reading and writing occur in non-blocking mode (implemented using java.nio.channels.Selector)
3. `ASYNCHRONOUS` - any interaction with the client occurs in asynchronous mode,
handlers (java.nio.channels.CompletionHandler) are executed in a fixed-size thread pool

Tasks from clients are executed in a shared fixed-size thread pool.

Other options:

* `tasks_threads_number` - the number of threads in the common 
thread pool in which tasks (sorting) from the client are executed;
this parameter is set at server startup, default value is 10
* you can specify a directory name for writing test results,
default value is the current directory name
* you can also specify the hostname, default value is `localhost`

Measured characteristics:

`task execution time` - sort time on the server

`client process time` - time between receive request from a client and send response him

`request average time` - average time between send request and receive response from the server + `delta`

## How to execute

1. start all servers with the command:
   * `./run_application.sh [tasks_threads_number]` where `tasks_threads_number` is an optional argument with a default value of 10 
2. run the application:
   * `./run_application.sh`

## Example

![](./pictures/example.png)

## Results

### Various array size (N)

`data: ./results/N`

![](./pictures/N_1.png)

![](./pictures/N_2.png)

![](./pictures/N_3.png)

### Various clients count (M)

`data: ./results/M`

![](./pictures/M_1.png)

![](./pictures/M_2.png)

![](./pictures/M_3.png)

### Various time delta between requests (delta)

`data: ./results/delta`

![](./pictures/delta_1.png)

![](./pictures/delta_2.png)

![](./pictures/delta_3.png)

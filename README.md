# Producer-Consumer
Implemented using Java

This program is a simple implementation of the muilti-process synchronization Producer-Consumer problem. The user will enter on the command line the sleep time, number of Producer threads, and number of consumer threads; Example: 20 10 5. And then the program will create the necessary number of producer and consumer threads and then place 100 random integers into a bounded buffer that has a number of slots to hold data, and then sleep for a random amount of time between 0 and 0.5 seconds. The mutex is used to protect the buffer and empty and full are used to make sure that the consumer does not remove data from an empty array. This program will run for 20 seconds or howerver long is specified by the user.

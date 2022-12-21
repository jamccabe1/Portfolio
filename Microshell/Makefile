# This is a very simple makefile
#

CC = gcc
CFLAGS = -g -Wall

ush: ush.o builtin.o expand.o
	$(CC) $(CFLAGS) -o ush ush.o builtin.o expand.o

ush.o: ush.c
	$(CC) -g -c ush.c

builtin.o: builtin.c builtin.h

expand.o: expand.c builtin.h


clean:
	rm *.o ush

/** Jacob McCabe
 * CSCI 347, Fall 2022
 */

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

static int check;

//Built in commands
void exit(int status);

int envset(char **argv) {
	return setenv(argv[1], argv[2], 1);
}

int envunset(char **argv) {
	return unsetenv(argv[1]);
}

int cd(char **argv) {
	return chdir(argv[1]);
}



/* Check if command is in the list of built-ins.
 * Updates static global for later use in run_built_in()
 */
int check_built_in(char **argv, int argc) {
	char *builtIns[] = {"exit", "envset", "envunset", "cd"};
	int size = sizeof(builtIns)/sizeof(builtIns[0]);

	int i = 0;
	while (i < size) {
		if (!strcmp(argv[0], builtIns[i]))
			check = i+1;

		i++;
	}
	return check;	
}

/* Execute a built in command
 */
void run_built_in(char **argv, int argc) {
	if (check == 1) {
		if (argc == 1) {
			exit(0);
		}
		exit(atoi(argv[1]));
	}
	
	int (*builtin_ptr[])(char**) = {envset, envunset, cd};

	int flag = (*builtin_ptr[check-2])(argv);
	if (flag == -1) {
		fprintf(stderr, "Error with function called.\n");
	}
	check = 0;	
}

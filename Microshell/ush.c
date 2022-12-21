/* CS 352 -- Micro Shell!  
 *
 *   Sept 21, 2000,  Phil Nelson
 *   Modified April 8, 2001 
 *   Modified January 6, 2003
 *   Modified January 8, 2017
 *
 *   Jacob McCabe
 *   CSCI 347
 */

#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/wait.h>
#include "builtin.h"

/* Constants */ 

#define LINELEN 1024

/* Prototypes */

void processline (char *line);
char **arg_parse(char *line, int *argcptr);

/* Shell main */

int
main (void)
{
	char   buffer [LINELEN];
	int    len;

	while (1) {
		/* prompt and get line */
		fprintf (stderr, "%% ");
		if (fgets (buffer, LINELEN, stdin) != buffer)
		break;

		/* Get rid of \n at end of buffer. */
		len = strlen(buffer);
		if (buffer[len-1] == '\n')
		buffer[len-1] = 0;

		/* Run it ... */
		processline (buffer);

	}

	if (!feof(stdin))
		perror ("read");

	return 0;		/* Also known as exit (0); */
}


void processline (char *line)
{
	pid_t  cpid;
	int    status;
  	
	//Handle expand here
	char new[LINELEN];
	int result = expand(line, new, LINELEN);
	if (result != 0) {
		fprintf(stderr, "Error expanding\n");
		return;
	}

	int argc = 0;
	char**parsed = arg_parse(new, &argc);
	/* Start a new process to do the job. */
	if (parsed[0] == NULL) { 
		free(parsed);
		return;
	}

	//check if parsed[0] is a built in command
	int check = check_built_in(parsed, argc);
	if (check != 0) {
		run_built_in(parsed, argc);
	}
	else {
		cpid = fork();	
		if (cpid < 0) {
			/* Fork wasn't successful */
			perror ("fork");
			return;
		}
	    
		/* Check for who we are! */
		if (cpid == 0) {
		/* We are the child! */
			execvp(parsed[0], parsed);
			/* exec reurned, wasn't successful */
			perror ("execvp");
			fclose(stdin);  // avoid a linux stdio bug
			exit (127);
		}
	    
		/* Have the parent wait for child to complete */
		if (wait (&status) < 0) {
		/* Wait wasn't successful */
			perror ("wait");
		}
	}
	free(parsed);
}

/* Argument Line Parser
 * Arguments:
 * line points to the command to be processed.
 * argcptr points to the number of args found in the line.
 *
 * Return: 
 * a pointer to a malloced array where each pointer corresponds to the first char
 * of each argument.
 * assigns the number of arguments in the line to argcptr.
 *
 * Note:
 * Consecutive spaces are ignored.
 */ 
char **arg_parse(char *line,  int *argcptr) {
	int argc = 0;
	int i = 0;
	int len = strlen(line);
	int loc[len];
	int bytes = sizeof(char *);
	int quotes = 0;

	i = 0;
	while (i < len) {
		//change spaces between args to null
		if (line[i] == 0x20) {
			line[i] = 0x00;
		}
		//Found a quote
		if (line[i] == 0x22) {
			quotes++;
			//new arg condition
			if (line[i-1] == 0x00) {
				memmove(&line[i], &line[i+1], len-i+1);
				loc[argc] = i;
				argc++;
			}
			//not a new arg
			else if(line[i-1] != 0x00) {
				memmove(&line[i], &line[i+1], len - i);
				i--;
			}

			i++;
			while (quotes % 2 == 1 && i < len) {
				//end of arg condition
				if (line[i] == 0x22 && line[i+1] == 0x20) {
					line[i] = 0x00;
					quotes++;
				}
				//not the end of an arg
				else if (line[i] == 0x22 && line[i+1] != 0x20) {
					quotes++;
					memmove(&line[i], &line[i+1], len - i);
					i--;
				}
				i++;
			}
		}

		else if (line[i-1] == 0x00 && line[i] != 0x00) {
			loc[argc] = i;
			argc++;
		}

		else if (i == 0 && line[i] != 0x00) {
			loc[argc] = i;
			argc++;
		}
		i++;
	}

	if (quotes % 2 == 1) {
		fprintf(stderr, "Odd number of quotes.\n");
		
	}


	char **parsed = malloc(bytes*(argc+1));
	if (parsed == NULL) {
		printf("Malloc Error\n");
	}


	i = 0;
	int index;
	while (i < argc)  {
		index = loc[i];
		parsed[i] = &line[index];
		i++;
	}
	parsed[argc] = NULL;


	*argcptr = argc;
	return parsed;
}


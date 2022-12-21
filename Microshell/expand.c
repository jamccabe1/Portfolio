#include "builtin.h"
#include <string.h>
#include <stdlib.h>
#include <stdio.h>

/** Expand line
 * This function looks for '${ ... }' in the input. When found,
 * it will search for the value associated with the specified
 * environment variable. 
 * 
 * returns 0 on success, -1 on failure.
 *
 * NOTE: i is index for char* orig,
 *       j is index for char* new,
 *       k is index for envVar, a temp storage for env variable.
 */
int expand(char *orig, char *new, int newsize) {
	int i = 0;
	int j = 0;
	int origLen = strlen(orig);
	char envVar[newsize];
	while (i < origLen) {
		//found ${
		if (orig[i] == 0x24 && orig[i+1] == 0x7B) {
			i += 2;
			int k = 0;
			//Until } found, copy env var into buf
			while (i < origLen && orig[i] != 0x7D) {
				envVar[k] = orig[i];
				k++;
				i++;
				if (i == origLen) {
					fprintf(stderr, "No } found\n");
					return -1;
				}
			}
			//found }
			if (orig[i] == 0x7D) {
				i++;
				envVar[k] = 0x00;
				char* buf = getenv(envVar);
				if (buf == NULL) {
					fprintf(stderr, "Not an Environment Variable\n");
					return -1;
				}
				int buflen = strlen(buf);

				if (newsize < buflen + k) {
					fprintf(stderr, "Buffer Overflow\n");
					return -1;
				}
				int l = 0;
				while (l<buflen) {
					new[j] = buf[l];
					l++;
					j++;
				}
			}
		}
		//copy
		else {
			new[j] = orig[i];
			i++;
			j++;
		}
	}
	new[j] = 0x00;
	return 0;
}

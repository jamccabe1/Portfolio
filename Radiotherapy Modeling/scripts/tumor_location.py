import numpy as np
import matplotlib.pyplot as plt

case1 = [[0 for i in range(51)] for j in range(51)]
case2 = [[0 for i in range(51)] for j in range(51)]

#Place 1's in tumor location, .5's in organ location, 0's else
#A[i,j]

            
for i in range(len(case1)):
    for j in range(len(case1)):
        if (i<5 or i>45):
            case1[i][j] = 0
        elif (j < 5):
            case1[i][j] = 0
        elif (j >45):
            case1[i][j] = 0
        elif (j>=35 and j<=45):
            case1[i][j] = 0.5
        else:
            case1[i][j] = 1
case1 = np.array(case1)

np.savetxt("case1.txt", case1)
#plt.imshow(case1)
#plt.show()


for i in range(len(case2)):
    for j in range(len(case2)):
        if (i<5 or i>45):
            case2[i][j] = 0
        elif (j < 5):
            case2[i][j] = 0
        elif (j >45):
            case2[i][j] = 0
        elif (j>=35 and j<=45 and i>=15 and i<=35):
            case2[i][j] = 0.5
        else:
            case2[i][j] = 1
case1 = np.array(case2)

np.savetxt("case2.txt", case2)
#plt.imshow(case2)
#plt.show()
import numpy as np
import matplotlib.pyplot as plt

def get_rot_mat(t, deg=False):
    if deg:
        t = np.deg2rad(t)
    return np.array([
        [np.cos(t), -np.sin(t)],
        [np.sin(t),  np.cos(t)]
    ])

def x_index(x, N):
    return x + N/2

def y_index(y, N):
    return -(y - N/2)

def bad_index(xi, yi, N):
    """ true means bad index """
    if xi < 0 or yi < 0:
        return True
    if xi >= N or yi >= N:
        return True
    return False


# put the output of rick's code into an array
with open('dose.txt', 'r') as og_data_file:
    og_data = []
    for i, line in enumerate(og_data_file):
        l = line.split(' ')
        line_list = []
        for j, n in enumerate(l):
            line_list.append(float(n))
        og_data.append(line_list)
og_data = np.array(og_data)

# figure out coordinates to center the data
N = len(og_data)
offset = N / 2
x_range = np.linspace(-offset, offset, N)
y_range = np.linspace(offset, -offset, N)
diff = x_range[1] - x_range[0]
# loops the x coords (len(og_data)) times
x_coords = np.tile(x_range, N)
# repeats each y coord (len(og_data)) times
y_coords = np.repeat(y_range, N)


empty_data = np.zeros((N, N))
# rotate the data by some angle
# angles = [0, 15, 30, 60, 90, 120, 134, 200, 280, 342]  # degrees btw
amt = 30
angles = [amt*i for i in range(360 // amt)]

#We want to be able to modify how far around the tumor we rotate
portion = len(angles)//2      ##Can modify how much of circle to include by changing denominator
new_angles = angles[0:portion]

# angles = [30]
for a in new_angles:
    rot_mat = get_rot_mat(a, deg=True)
    # go through each coordinate and rotate it by "a" degrees
    # then round to the nearest coordinate
    # i counts the column, j counts the row
    for i, x in enumerate(x_range):
        for j, y in enumerate(y_range):
            c = np.array([x, y])
            val = og_data[j][i]
            rotated_c = rot_mat @ c
            # get the indices for the 51x51 matrix.
            # these indices are based on where the x/y coords went
            # after we rotated them.
            # i realize this part is a bit cryptic
            # it's converting from the centered picture back to the
            # one with the origin at the top left corner.
            # the i*(diff - 1) is a bit of jank to fix the fact that
            # the intervals aren't 1, they're ~1.02.
            xi = round(x_index(rotated_c[0], N) - (i*(diff - 1)))
            yi = round(y_index(rotated_c[1], N) - (j*(diff - 1)))
            # if the index is negative or outside the picture, ignore it
            if bad_index(xi, yi, N):
                continue
            empty_data[yi][xi] += val

plt.imshow(empty_data)
plt.show()
np.savetxt("added_doses_at_30_deg_intervals.txt", empty_data)

# create the original plot but centered on the (x,y) axis
# ax = plt.gca()
# ax.set_aspect(1)
# plt.scatter(x_coords, y_coords, c=og_data, marker='s')
# plt.show()
import numpy as np
import matplotlib.pyplot as plt #optional for plotting

def get_max_value(data):
    #max = [row, col]
    max = [-1,-1]
    for i in range(len(data)):
        for j in range(len(data)):
            if data[i,j] > data[max[0], max[1]]:
                max = [i,j]
    return max

def normalize(data, max):
    for i in range(len(data)):
        for j in range(len(data)):
            data[i,j] = data[i,j] / max
    return data
    
def comp_death_prob(data, p):
    for i in range(len(data)):
        for j in range(len(data)):
            data[i,j] = geom_cdf(data[i,j], p)
    return data

def geom_cdf(n, p):
    return 1 - (1-p)**n

#Read in the single beam data
with open('dose.txt', 'r') as og_data_file:
    og_data = []
    for i, line in enumerate(og_data_file):
        l = line.split(' ')
        line_list = []
        for j, n in enumerate(l):
            line_list.append(float(n))
        og_data.append(line_list)
og_data = np.array(og_data)

#Find the max dose for our benchmark value
max_dose_loc = get_max_value(og_data)
max_dose = og_data[max_dose_loc[0], max_dose_loc[1]]
#Normalizing OG data was just for testing
norm_data = normalize(og_data, max_dose)

#Read in the body diagram
with open('case2.txt', 'r') as body_file:#Change file depending on case
    body_data = []
    for i, line in enumerate(body_file):
        l = line.split(' ')
        line_list = []
        for j, n in enumerate(l):
            line_list.append(float(n))
        body_data.append(line_list)
body_data = np.array(body_data)

#Read in rotated data
#Can use a different file too, just change the .txt file
with open('added_doses_at_30_deg_intervals.txt', 'r') as data_file:
    rot_data = []
    for i, line in enumerate(data_file):
        l = line.split(' ')
        line_list = []
        for j, n in enumerate(l):
            line_list.append(float(n))
        rot_data.append(line_list)
rot_data = np.array(rot_data)
#normalize wrt benchmark value
norm_rot = normalize(rot_data, max_dose)

norm_rot = np.rint(norm_rot)
#now compute prob of death
p = 0.5  ###CHOOSE YOUR PROB###
death = comp_death_prob(norm_rot, p)
death = np.multiply(death, body_data)

#np.savetxt("death_prob_gen.txt", death)
#plt.imshow(death)
#plt.show()
total = 0.0
heal_tot=0.0
#Uncomment for which thing you want
#For total area
#for i in range(len(death)):
#    for j in range(len(death)):
#        total += (1 - death[i,j])
        
#For adjacent tumor
#tumor area
#for i in range(5, 45):
#    for j in range(5, 34):
#        total += (1 - death[i,j])
#heathy organ area
#for i in range(5,45):
#    for j in range(35,45):
#        heal_tot+=(1-death[i,j])
 
#For partially surrounding tumor:
#tumor area
#for i in range(5,14):
#    for j in range(5,45):
#        total += (1 - death[i,j])
#for i in range(36,45):
#    for j in range(5,45):
#        total += (1 - death[i,j])
#for i in range(15,35):
#    for j in range(5,34):
#        total += (1 - death[i,j])
#healthy organ area
#for i in range(15,35):
#    for j in range(35,45):
#        heal_tot +=(1 - death[i,j])
case1_num_cells = 1160
case1_heal_cells = 400
case2_num_cells = 1300
case2_heal_cells = 200
num_cells = len(death)**2
exp_surv = total / case2_num_cells
exp_heal_surv = heal_tot / case2_heal_cells
#print(exp_surv)
#print(exp_heal_surv)

library(nnet)
library(dplyr)
library(ggplot2)

features = read.csv('MLR/bug_data_features.csv', header=T)
targets = read.csv('MLR/bug_data_targets.csv', header=T)

targ1 = read.csv('MLR/bug_data_targets_WEEKvsREST.csv', header=T)
targ2 = read.csv('MLR/bug_data_targets_QTRvsREST.csv', header=T)
targ3 = read.csv('MLR/bug_data_targets_SCHYRvsREST.csv', header=T)
targ4 = read.csv('MLR/bug_data_targets_LONGvsREST.csv', header=T)
targ = targ1 + targ2+targ3
glimpse(targ)
data = cbind(features, targ)
data = data[,c(-1,-2)]

glimpse(data)


##################
#
# CLEANING CONTINUOUS DATA
#
##################

####VISUAL ANALYSIS, DROPPING OUTLIERS
#continuous <- select_if(data, is.numeric)
continuous <- data[,-6]
summary(continuous)

#length_desc,length_steps_to_repro, length_additional_info, and bug_actions
#have large outliers. So we remove the top 1%
top_one_desc = quantile(data$length_desc,0.99) #quantile(data, percentile)
top_one_desc

top_one_steps = quantile(data$length_steps_to_repro, 0.99)
top_one_steps

top_one_info = quantile(data$length_additional_info, 0.99)
top_one_info

top_one_actions = quantile(data$bug_actions, 0.99)
top_one_actions

data_drop = data %>%  
  filter(length_desc < top_one_desc)

data_drop = data_drop %>%
  filter(length_steps_to_repro < top_one_steps)

data_drop = data_drop %>%
  filter(length_additional_info < top_one_info)

data_drop = data_drop %>%
  filter(bug_actions < top_one_actions)

dim(data_drop)

#Since the scale is so different for everything, we want to standardize it
temp = data_drop$time_to_resolve
data_rescale = data_drop %>%
  mutate_if(is.numeric, funs(as.numeric(scale(.))))
head(data_rescale)
data_rescale$time_to_resolve = temp

##Lets look at graphs again
continuous_scale <- select_if(data_rescale, is.numeric)
summary(continuous_scale)

factor <- data.frame(data_rescale$time_to_resolve)
ncol(factor)
graph <- lapply(names(factor),
                function(x) 
                  ggplot(factor, aes(get(x))) +
                  geom_bar() +
                  theme(axis.text.x = element_text(angle = 90)))
graph
table(data_rescale$time_to_resolve)



##################
#
# TRAIN/TEST
#
##################
precision <- function(matrix) {
  # True positive
  tp <- matrix[2, 2]
  # false positive
  fp <- matrix[1, 2]
  return (tp / (tp + fp))
}
recall <- function(matrix) {
  # true positive
  tp <- matrix[2, 2]# false positive
  fn <- matrix[2, 1]
  return (tp / (tp + fn))
}

set.seed(1111)
create_train_test <- function(data, size = 0.8, train = TRUE) {
  n_row = nrow(data)
  total_row = size * n_row
  train_sample <- 1: total_row
  if (train == TRUE) {
    return (data[train_sample, ])
  } else {
    return (data[-train_sample, ])
  }
}
data_train <- create_train_test(data_rescale, 0.7, train = TRUE)
data_test <- create_train_test(data_rescale, 0.7, train = FALSE)
dim(data_train)
dim(data_test)


#BASELINE COPARISON FOR MODELS (guessing majority class)
tab = table(data_test$time_to_resolve)
table(data_train$time_to_resolve) #majority is class 1
baseline = tab[2]/ (tab[1] + tab[2])
baseline

mylogit <- glm( 
  formula = time_to_resolve ~  length_summary + length_desc + length_steps_to_repro 
  + length_additional_info, 
  data = data_train, family = 'binomial'
)
summary(mylogit) #steps has super high p-value so we're going modify it into one of the nonlinear terms we found (bug*steps - see mylogit3)
predict <- predict(mylogit, data_test, type = 'response')
table_mat <- table(data_test$time_to_resolve, predict > 0.5)
table_mat
accuracy_Test <- sum(diag(table_mat)) / sum(table_mat)
accuracy_Test
prec <- precision(table_mat)
prec
rec <- recall(table_mat)
rec
f1 <- 2 * ((prec * rec) / (prec + rec))
f1


mylogit2 <- glm( 
  formula = time_to_resolve ~ length_summary + bug_actions + length_desc + length_additional_info, 
  data = data, family = 'binomial'
)
summary(mylogit2)
predict <- predict(mylogit2, data_test, type = 'response')
table_mat <- table(data_test$time_to_resolve, predict > 0.5)
accuracy_Test <- sum(diag(table_mat)) / sum(table_mat)
accuracy_Test
prec <- precision(table_mat)
rec <- recall(table_mat)
f1 <- 2 * ((prec * rec) / (prec + rec))
f1


#The terms with potential: desc&bug, steps&bug, steps&summary, steps&desc, and steps & add'l

### THis is the one  + length_steps_to_repro
mylogit3 <- glm( 
  formula = time_to_resolve ~  length_summary + length_desc  
  + length_additional_info +length_steps_to_repro*bug_actions, 
  data = data_train, family = 'binomial'
)
summary(mylogit3)
predict <- predict(mylogit3, data_test, type = 'response')
table_mat <- table(data_test$time_to_resolve, predict > 0.5)
table_mat
accuracy_Test <- sum(diag(table_mat)) / sum(table_mat)
accuracy_Test
prec <- precision(table_mat)
prec
rec <- recall(table_mat)
rec
f1 <- 2 * ((prec * rec) / (prec + rec))
f1
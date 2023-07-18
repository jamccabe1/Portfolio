library(ggplot2)
library(ggpubr)

features = read.csv('MLR/bug_data_features.csv', header=T)
targets = read.csv('MLR/bug_data_targets_categ.csv', header=T)

data = cbind(features, targets)
data = data[,c(-1,-2)]

glimpse(data)

#####################
#
# CONTINUOUS
#
#####################

####VISUAL ANALYSIS, DROPPING OUTLIERS
#continuous <- select_if(data, is.numeric)
continuous <- data[,-6]
summary(continuous)

p1 = ggplot(continuous, aes(x=length_summary)) + geom_density(alpha=.2, fill="#FF6666") + ylab("") + xlab("Summary length")

#length_desc,length_steps_to_repro, length_additional_info, and bug_actions
#have large outliers. So we remove the top 1%
p2 = ggplot(continuous, aes(x=length_desc)) + geom_density(alpha=.2, fill="#FF6666")+ ylab("") + xlab("Description length")
top_one_desc = quantile(data$length_desc,0.99) #quantile(data, percentile)
top_one_desc

p3 = ggplot(continuous, aes(x=length_steps_to_repro)) + geom_density(alpha=.2, fill="#FF6666")+ ylab("") + xlab("Steps length")
top_one_steps = quantile(data$length_steps_to_repro, 0.99)
top_one_steps

p4 = ggplot(continuous, aes(x=length_additional_info)) + geom_density(alpha=.2, fill="#FF6666")+ ylab("") + xlab("Addt'l Info length")
top_one_info = quantile(data$length_additional_info, 0.99)
top_one_info

p5 = ggplot(continuous, aes(x=bug_actions)) + geom_density(alpha=.2, fill="#FF6666")+ ylab("") + xlab("Actions")
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
p6 = ggplot(continuous_scale, aes(x=length_summary)) + geom_density(alpha=.2, fill="#FF6666")+ ylab("") + xlab("Summary length")
p7 = ggplot(continuous_scale, aes(x=length_desc)) + geom_density(alpha=.2, fill="#FF6666")+ ylab("") + xlab("Description length")
p8 = ggplot(continuous_scale, aes(x=length_steps_to_repro)) + geom_density(alpha=.2, fill="#FF6666")+ ylab("") + xlab("Steps length")
p9 = ggplot(continuous_scale, aes(x=length_additional_info)) + geom_density(alpha=.2, fill="#FF6666")+ ylab("") + xlab("Addt'l Info length")
p10 = ggplot(continuous_scale, aes(x=bug_actions)) + geom_density(alpha=.2, fill="#FF6666")+ ylab("") + xlab("Actions")



ggarrange(p1,p6,p2,p7,p3,p8,p4,p9,p5,p10,
         ncol=2, nrow=5)


##################
#
# CORRELATION
#
##################

library(GGally)
# Convert data to numeric
corr <- data.frame(lapply(data_rescale, as.integer))
# Plot the graph
ggcorr(corr,
       method = c("pairwise", "spearman"),
       nbreaks = 6,
       hjust = 0.8,
       label = TRUE,
       label_size = 3,
       color = "grey50")


#####################
#
# CATEGORICAL
#
#####################
#looking at time_to_resolve (categorical)
factor <- data.frame(data_rescale$time_to_resolve)
ncol(factor)
graph <- lapply(names(factor),
                function(x) 
                  ggplot(factor, aes(get(x)) +
                  geom_bar() +
                  theme(axis.text.x = element_text(angle =0))))

graph



p1 = ggplot(data_rescale, aes(x = length_summary)) +
  geom_density(aes(color = time_to_resolve), alpha = 0.5) +
  theme_classic()
p2 = ggplot(data_rescale, aes(x = length_desc)) +
  geom_density(aes(color = time_to_resolve), alpha = 0.5) +
  theme_classic()
p3 = ggplot(data_rescale, aes(x = length_steps_to_repro)) +
  geom_density(aes(color = time_to_resolve), alpha = 0.5) +
  theme_classic()
p4 = ggplot(data_rescale, aes(x = length_additional_info)) +
  geom_density(aes(color = time_to_resolve), alpha = 0.5) +
  theme_classic()
p5 = ggplot(data_rescale, aes(x = bug_actions)) +
  geom_density(aes(color = time_to_resolve), alpha = 0.5) +
  theme_classic()
ggarrange(p1,p2,p3,p4,p5, ncol=1, nrow=5)

anova1 <- aov(length_summary~time_to_resolve, data_rescale)
summary(anova1)
anova2 <- aov(length_desc~time_to_resolve, data_rescale)
summary(anova2)
anova3 <- aov(length_steps_to_repro~time_to_resolve, data_rescale)
summary(anova3)
anova4 <- aov(length_additional_info~time_to_resolve, data_rescale)
summary(anova4)
anova5 <- aov(bug_actions~time_to_resolve, data_rescale)
summary(anova5)
#Statistical significance between groups for length_desc/time and bug_actions/time
# less strong for length_summary/time and length_additional_info/time
# This supports the hypothesis that there is a large difference in average between groups
# for length of description and bug actions. Not as strong for length of summary and additional info

data_recast <- data_rescale %>%
  mutate(time_to_resolve = factor(ifelse(time_to_resolve=="Week" | time_to_resolve=="Quarter", "Within a quarter", "More than a quarter")))
factor <- data.frame(data_rescale$time_to_resolve)
ncol(factor)
graph <- lapply(names(factor),
                function(x) 
                  ggplot(factor, aes(get(x)) +
                           geom_bar() +
                           theme(axis.text.x = element_text(angle =0))))

graph
table(data_recast$time_to_resolve)

p1 = ggplot(data_recast, aes(x = length_summary)) +
  geom_density(aes(color = time_to_resolve), alpha = 0.5) +
  theme_classic()
p2 = ggplot(data_recast, aes(x = length_desc)) +
  geom_density(aes(color = time_to_resolve), alpha = 0.5) +
  theme_classic()
p3 = ggplot(data_recast, aes(x = length_steps_to_repro)) +
  geom_density(aes(color = time_to_resolve), alpha = 0.5) +
  theme_classic()
p4 = ggplot(data_recast, aes(x = length_additional_info)) +
  geom_density(aes(color = time_to_resolve), alpha = 0.5) +
  theme_classic()
p5 = ggplot(data_recast, aes(x = bug_actions)) +
  geom_density(aes(color = time_to_resolve), alpha = 0.5) +
  theme_classic()
ggarrange(p1,p2,p3,p4,p5, ncol=1, nrow=5)


anova1 <- aov(length_summary~time_to_resolve, data_recast)
summary(anova1)
anova2 <- aov(length_desc~time_to_resolve, data_recast)#sorta SIGNIFICANT
summary(anova2)
anova3 <- aov(length_steps_to_repro~time_to_resolve, data_recast)
summary(anova3)
anova4 <- aov(length_additional_info~time_to_resolve, data_recast)
summary(anova4)
anova5 <- aov(bug_actions~time_to_resolve, data_recast)#SIGNIFICANT
summary(anova5)
# This supports the hypothesis that there is a large difference in average between groups
# for length of description and bug actions. Not as strong for length of summary and additional info
# All of them except for 'steps to reproduce' have a p-value < 0.05. This means that we can reject
# the null hypothesis that the averages for each category of resolution are not distinct.


###################
#
# NONLINEARITY / INTERACTION TERMS for Log Reg
#
###################



ggplot(data_recast, aes(x = length_summary, y = bug_actions)) +
  geom_point(aes(color = time_to_resolve),
             size = 0.5) +
  stat_smooth(method = 'lm',
              formula = y~poly(x, 2),
              se = TRUE,
              aes(color = time_to_resolve)) +
  theme_classic()

#POTENTIALLY GOOD -- BEST ONE
nl1 = ggplot(data_recast, aes(x = length_desc, y = bug_actions)) +
  geom_point(aes(color = time_to_resolve),
             size = 0.5) +
  stat_smooth(method = 'lm',
              formula = y~poly(x, 2),
              se = TRUE,
              aes(color = time_to_resolve)) +
  theme_classic()

#POTENTIALLY GOOD
nl2 = ggplot(data_recast, aes(x = length_steps_to_repro, y = bug_actions)) +
  geom_point(aes(color = time_to_resolve),
             size = 0.5) +
  stat_smooth(method = 'lm',
              formula = y~poly(x, 2),
              se = TRUE,
              aes(color = time_to_resolve)) +
  theme_classic()

nl3 = ggplot(data_recast, aes(x = length_additional_info, y = bug_actions)) +
  geom_point(aes(color = time_to_resolve),
             size = 0.5) +
  stat_smooth(method = 'lm',
              formula = y~poly(x, 2),
              se = TRUE,
              aes(color = time_to_resolve)) +
  theme_classic()

ggplot(data_recast, aes(x = length_desc, y = length_summary)) +
  geom_point(aes(color = time_to_resolve),
             size = 0.5) +
  stat_smooth(method = 'lm',
              formula = y~poly(x, 2),
              se = TRUE,
              aes(color = time_to_resolve)) +
  theme_classic()

ggplot(data_recast, aes(x = length_additional_info, y = length_summary)) +
  geom_point(aes(color = time_to_resolve),
             size = 0.5) +
  stat_smooth(method = 'lm',
              formula = y~poly(x, 2),
              se = TRUE,
              aes(color = time_to_resolve)) +
  theme_classic()

ggplot(data_recast, aes(x = length_steps_to_repro, y = length_summary)) +
  geom_point(aes(color = time_to_resolve),
             size = 0.5) +
  stat_smooth(method = 'lm',
              formula = y~poly(x, 2),
              se = TRUE,
              aes(color = time_to_resolve)) +
  theme_classic()

ggplot(data_recast, aes(x = length_steps_to_repro, y = length_desc)) +
  geom_point(aes(color = time_to_resolve),
             size = 0.5) +
  stat_smooth(method = 'lm',
              formula = y~poly(x, 2),
              se = TRUE,
              aes(color = time_to_resolve)) +
  theme_classic()

ggplot(data_recast, aes(x = length_additional_info, y = length_desc)) +
  geom_point(aes(color = time_to_resolve),
             size = 0.5) +
  stat_smooth(method = 'lm',
              formula = y~poly(x, 2),
              se = TRUE,
              aes(color = time_to_resolve)) +
  theme_classic()

ggplot(data_recast, aes(x = length_steps_to_repro, y = length_additional_info)) +
  geom_point(aes(color = time_to_resolve),
             size = 0.5) +
  stat_smooth(method = 'lm',
              formula = y~poly(x, 2),
              se = TRUE,
              aes(color = time_to_resolve)) +
  theme_classic()

#The terms with potential: desc&bug, steps&bug
#The one to pay attention to most is desc&bug because it has the most distinct curves (least overlap of shadow)
ggarrange(nl1,nl2,nl3, ncol=1, nrow=3)

library(ggplot2)
library(emoa)
library(GGally)
library(reshape2)
clip <- inbounds

setwd('/Users/thijs/Desktop/export')
source('load.R')

d <- blocks$p_log.avg_sentence_length...2.5.
plot_histogram(d,blocks)
plot_probability_estimate(d,  r = 0.2, data=blocks)
mean(d)
sd(d)
min(d)
max(d)









#########################################################
### HTML tags
tags <- names(blocks)[grepl("tag_",names(blocks))]

# For blocks
counts <- sapply(tags, function (tag) {
  subset <- blocks[blocks[,tag]==1,]
  c(sum(subset$label==1)/length(subset$label), length(subset$label))
})
counts <- t(counts)
counts <- data.frame(counts)
colnames(counts) <- c("probability","count")
counts <- counts[counts$count > 0,]
head(counts[order(counts$count, decreasing=T),],n=20)

# For edges
tags <- names(edges)[grepl("common_ancestor_tag_",names(edges))]
counts <- sapply(tags, function (tag) {
  subset <- edges[edges[,tag]==1,]
  n <- length(subset$label)
  a <- sum(subset$label==0)/n
  b <- sum(subset$label==1)/n
  c <- sum(subset$label==10)/n
  d <- sum(subset$label==11)/n
  c(sd(c(a,b,c,d)), n)
})
counts <- t(counts)
counts <- data.frame(counts)
colnames(counts) <- c("sd","count")
counts <- counts[counts$count > 0,]
head(counts[order(counts$count, decreasing=T),],n=20)


# Tree distance
d <- clip(edges$tree_distance,2,5)
plot_histogram(d, data=edges)
summary(d)

tab <- table(data.frame(label=edges$label,distance=clip(edges$tree_distance,0,5)))
tab <- sweep(tab, 2, colSums(tab), FUN="/")
dta <- melt(tab)
ggplot(dta, 
       aes(
         x = factor(distance),
         y = value,
         fill = factor(label,levels=c(0,1,10,11),labels=c("00","01","10","11"))
         )) + geom_bar(stat="identity") + coord_flip() +
         scale_fill_discrete(name = "Label") +
         scale_x_discrete(name = "Tree distance") +
         scale_y_continuous(name = "Probability")







# Punctuation ratio
subset <- dta[dta$contains_punctuation == 1,]
d <- subset$log.punctuation_ratio.
multiplot(plot_histogram(d, subset),
plot_probability_estimate(d, data=subset, r=0.2))
mean(d)
sd(d)



# Words
subset <- dta[dta$has_word == 1,]
d <- subset$log.n_words.
multiplot(plot_histogram(d, data=subset),
          plot_probability_estimate(d, data=subset, r=0.4))
mean(d)
sd(d)



# Word length
subset <- dta[dta$has_word == 1,]
d <- clip(subset$avg_word_length,3,15)
multiplot(plot_histogram(d, data=subset),
          plot_probability_estimate(d, data=subset, r=5))
mean(d)
sd(d)



# Stopword ratio
subset <- dta[dta$has_word == 1 & dta$stopword_ratio > 0,]
d <- subset$stopword_ratio
multiplot(plot_histogram(d, data=subset),
          plot_probability_estimate(d, data=subset, r=.1))
mean(d)
sd(d)



# Numeric ratio
subset <- dta[dta$numeric_ratio > 0,]
d <- subset$numeric_ratio
multiplot(plot_histogram(d, data=subset),
          plot_probability_estimate(d, data=subset, r=.2))
mean(d)
sd(d)



# Sentences
mean(ifelse(dta$n_sentences>1,1,-1))
sd(ifelse(dta$n_sentences>1,1,-1))

subset <- dta
d <- clip(subset$n_sentences,0,10)
multiplot(plot_histogram(d, data=subset),
          plot_probability_estimate(d, data=subset, r=1))
mean(d)
sd(d)



# Sentence length
subset <- dta
d <- clip(log(subset$n_characters / subset$n_sentences),2,5)
multiplot(plot_histogram(d, data=subset),
          plot_probability_estimate(d, data=subset, r=.4))
mean(d)
sd(d)





# Position
subset <- dta
d <- subset$relative_position
multiplot(plot_histogram(d, data=subset),
          plot_probability_estimate(d, data=subset, r=.05))
mean(d)
sd(d)
mean(d^2)
sd(d^2)




# Words with capital
subset <- dta
d <- subset$ratio_words_with_capital
multiplot(plot_histogram(d, data=subset),
          plot_probability_estimate(d, data=subset, r=.001))
mean(d)
sd(d)

mean(d^2)
sd(d^2)

mean(d^3)
sd(d^3)






# N same words
subset <- dta
d <- ifelse(subset$n_same_word > 2,1,-1)
multiplot(plot_histogram(d, data=subset),
          plot_probability_estimate(d, data=subset, r=.1))
mean(d)
sd(d)

# N same class Paths
subset <- dta
d <- subset$n_same_class_path / subset$n_leaves
multiplot(plot_histogram(d, data=subset),
          plot_probability_estimate(d, data=subset, r=.3))
mean(d)
sd(d)



























multiplot <- function(..., plotlist=NULL, file, cols=1, layout=NULL) {
  require(grid)
  
  # Make a list from the ... arguments and plotlist
  plots <- c(list(...), plotlist)
  
  numPlots = length(plots)
  
  # If layout is NULL, then use 'cols' to determine layout
  if (is.null(layout)) {
    # Make the panel
    # ncol: Number of columns of plots
    # nrow: Number of rows needed, calculated from # of cols
    layout <- matrix(seq(1, cols * ceiling(numPlots/cols)),
                     ncol = cols, nrow = ceiling(numPlots/cols))
  }
  
  if (numPlots==1) {
    print(plots[[1]])
    
  } else {
    # Set up the page
    grid.newpage()
    pushViewport(viewport(layout = grid.layout(nrow(layout), ncol(layout))))
    
    # Make each plot, in the correct location
    for (i in 1:numPlots) {
      # Get the i,j matrix positions of the regions that contain this subplot
      matchidx <- as.data.frame(which(layout == i, arr.ind = TRUE))
      
      print(plots[[i]], vp = viewport(layout.pos.row = matchidx$row,
                                      layout.pos.col = matchidx$col))
    }
  }
}

plot_probability_estimate <- function (variable, varname = "Variable", r = 0.05, data=dta) {
  x = seq(min(variable),max(variable),length=100)
  estimatePercentage <- function(x) {
    mask = variable < x + r & variable > x - r
    mask2 = mask & data$label == 1
    n = sum(mask)
    if (n == 0) return(0) else return(sum(mask2) / n)
  }
  points = sapply(x, estimatePercentage)
  ggplot(data = data.frame(points), aes(x = x, y = points)) +
    geom_ribbon(aes(ymin = 0.5, ymax = points), fill = "grey70", alpha=0.3) +
    ylim(0, 1) +
    geom_hline(yintercept=0.5,color="grey70") +
    geom_line() +
    ylab("Probability of content") +
    xlab(varname)
}

plot_histogram <- function (variable, data = dta) {
  ggplot(data=data, aes(x = variable, fill=label)) + geom_histogram() # aes(y=..density..)
}


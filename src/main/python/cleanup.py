filename = "../../../public/authors.csv"
with open(filename) as f:
    lines = f.readlines()
    for line in lines:
        articleid = line.split(", ")[0]
        print("mv /Users/cesc/Desktop/hypefactors/AuthorExtractor/src/main/resources/all_the_news_dataset/html/" + articleid + ".html /Users/cesc/Desktop/hypefactors/AuthorExtractor/src/main/resources/all_the_news_dataset/html/clean")


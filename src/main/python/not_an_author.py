import os
import pandas as pd
import sys


def get_author(filename_hash, page_df):
    if filename_hash in page_df.index:
        return page_df.loc[filename_hash]['Author']
    else:
        return "NaN"


def clean_text(row):
    return [r.decode('unicode_escape').encode('ascii', 'ignore') for r in row]


def main():
    authors = "../../../public/authors.csv"
    html_path = "../../../public/train_and_test"
    pd.set_option('display.max_rows', None)
    page_df = pd.read_csv(authors, sep=";", header=0)
    page_df["Author"] = page_df.Author.str.replace('[^\x00-\x7F]', '')
    page_df.set_index("Hash", inplace=True)
    i=0
    file_list = [os.path.join(html_path, f) for f in os.listdir(html_path) if os.path.isfile(os.path.join(html_path, f))]
    for file in file_list:
        filename_hash = file.split("/")[5].split("_block_features")[0].replace("_", "")
        if get_author(filename_hash, page_df) in ["Associated Press", "Post Editorial Board", "NPR Staff", "Reuters", "Post Staff Report", "Editorial Board", "The Editors", "News.com.au"]:
            i += 1
            print(f"rm -R /Users/cesc/Desktop/hypefactors/AuthorExtractor/public/html/{filename_hash}.html")
            print(f"rm -R /Users/cesc/Desktop/hypefactors/AuthorExtractor/public/train_and_test/_{filename_hash}_block_features.csv")



if __name__ == '__main__':
    main()

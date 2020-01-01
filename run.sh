<<<<<<< HEAD
# 0a0a5bddf78b2ef2de13c71fbf737764bbf97449
# 0a1e33912ecfcb994e15caf56e5fc8d796c11e7c
#SBT_OPTS="-Xms512M -Xmx4096M -Xss2M -XX:MaxMetaspaceSize=1024M"  sbt "runMain ch.ethz.dalab.web2text/ExtractPageFeatures  src/main/resources/all_the_news_dataset/html/0a1e33912ecfcb994e15caf56e5fc8d796c11e7c.html src/main/resources/all_the_news_dataset/training/output_7_contains_author2"
# SBT_OPTS="-Xms512M -Xmx4096M -Xss2M -XX:MaxMetaspaceSize=1024M"  sbt "runMain ch.ethz.dalab.web2text/ExtractPageFeatures  src/main/resources/all_the_news_dataset/html/0a1e33912ecfcb994e15caf56e5fc8d796c11e7c.html src/main/resources/all_the_news_dataset/dom/output_7_"
SBT_OPTS="-Xms512M -Xmx4096M -Xss2M -XX:MaxMetaspaceSize=1024M"  sbt "runMain ch.ethz.dalab.web2text/ExtractCorpusFeatures  src/main/resources/all_the_news_dataset/html/ src/main/resources/all_the_news_dataset/dom/output_7_"
=======
sbt "runMain src/main/scala/ch.ethz.dalab.web2text/ExtractPageFeatures  src/main/resources/all_the_news_dataset/html/fff486af5b2591cc3b71e841459a221aacc97d77.html src/main/resources/all_the_news_dataset/dom/output_6_"
>>>>>>> e60ffe74ab9be9ba60321cd5d22fd3b9dbddf67a

SBT_OPTS="-Xms512M -Xmx4096M -Xss2M -XX:MaxMetaspaceSize=1024M"  sbt "runMain ch.ethz.dalab.web2text/ExtractCorpusFeatures  public/html/ public/train_and_test/"

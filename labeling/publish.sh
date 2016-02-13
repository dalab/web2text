meteor build . --architecture=os.linux.x86_64
scp labeling.tar.gz root@idvm-infk-hofmann04:/root
rm labeling.tar.gz
ssh root@idvm-infk-hofmann04 "(tar -xvf labeling.tar.gz && rm labeling.tar.gz)"
# ~/.bashrc
./scripts/initialize-election.sh working
./scripts/generate-and-encrypt-ballots.sh working 10
./scripts/tally-encrypted-ballots.sh working
./scripts/tally-decrypt.sh working
./scripts/make-mixnet-input.sh working
./scripts/mixnet-initialize.sh working
./scripts/mixnet-shuffle.sh working
./scripts/copy-to-bb.sh working
./scripts/mixnet-verify.sh working
./scripts/pep-compare.sh working
./scripts/pep-verify.sh working

# fetch latest eg library
# cd tools/electionguard/egk-webapps
# git fetch origin
# git rebase -i origin/main
# ./gradlew clean assemble
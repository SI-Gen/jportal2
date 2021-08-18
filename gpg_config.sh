git config --global gpg.program gpg2
git config --list
gpg2 --version

echo "PIN-ENTRY"
git clone git://git.gnupg.org/pinentry.git
pinentry --version

sudo apt-get install -y gpg-agent
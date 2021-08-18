git config --global gpg.program gpg2
git config --list
gpg2 --version
echo "PIN-ENTRY"
git clone git://git.gnupg.org/pinentry.git
pinentry --version
mkdir ~/.gnupg
echo 'default-cache-ttl 34560000' >> ~/.gnupg/gpg-agent.conf
echo 'max-cache-ttl 34560000' >> ~/.gnupg/gpg-agent.conf
echo 'pinentry-program' >> ~/.gnupg/gpg-agent.conf
echo 'use-agent' > ~/.gnupg/gpg.conf
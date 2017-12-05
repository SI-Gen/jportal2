echo "Installing jportal dependencies..."
sudo apt-get -y update
sudo apt-get install -y git
#sudo apt-get install -y cmake
#sudo apt-get install -y ant
sudo apt-get install -y openjdk-8-jdk
#sudo apt-get install -y eclipse
sudo apt-get install -y javacc
#sudo apt-get install -y coco-java
#sudo apt-get install -y log4j
#sudo apt-get install -y postgresql 

#Install IntelliJ IDEA
#wget --continue https://s3-eu-west-1.amazonaws.com/devops-static-downloads-public/ideaIC-2017.1.tar.gz
#tar xzvf ideaIC-2017.1.tar.gz

#Install some eclipse plugins
#cmakeed
#eclipse -nosplash -application org.eclipse.equinox.p2.director -repository http://download.eclipse.org/releases/indigo/,http://cmakeed.sourceforge.net/eclipse/ -installIU com.cthing.cmakeed.feature.feature.group

#pydev, first install the required cert, then install pydev
#sudo ./install_pydev_certificate.sh
#sudo eclipse -nosplash -application org.eclipse.equinox.p2.director -repository http://download.eclipse.org/releases/indigo/,http://pydev.org/updates/ -installIU org.python.pydev.feature.feature.group


#Oracle

#libAIO is required by oracle
#sudo apt-get install -y libaio-dev
#sudo apt-get install -y libaio1
 
#Alien is required to install the RPM packages
#sudo apt-get install -y --force-yes alien

#Alien can't install the oracle files if they reside on a vagrant shared folder, so copy them to ~/oracle_temp and install from there...
#mkdir ~/oracle_temp
#cp /main/jportal/oracle/* ~/oracle_temp/
#cd ~/oracle_temp
#sudo alien -i oracle-instantclient12.1-basiclite-12.1.0.2.0-1.x86_64.rpm
#sudo alien -i oracle-instantclient12.1-jdbc-12.1.0.2.0-1.x86_64.rpm 
#sudo alien -i oracle-instantclient12.1-devel-12.1.0.2.0-1.x86_64.rpm
#sudo alien -i oracle-instantclient*-sqlplus-*.rpm

#set oracle confs
#sudo sh -c "echo /usr/lib/oracle/12.1/client/lib >/etc/ld.so.conf.d/oracle.conf"
#sudo ldconfig

#TODO: Must be added to .bashrc??
export LD_LIBRARY_PATH=/usr/lib/oracle/12.1/client64/lib:$LD_LIBRARY_PATH

#Vince currently has a bug in the crackle stuff
#rm -Rf /home/vagrant/sync/generators/bbd/crackle

#Set up git user and email
git config --global --edit


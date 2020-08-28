#!/bin/bash

echo "Installing BCM2835 libraries"
wget http://www.airspayce.com/mikem/bcm2835/bcm2835-1.60.tar.gz
tar zxvf bcm2835-1.60.tar.gz
cd bcm2835-1.60/
sudo ./configure
sudo make
sudo make check
sudo make install
cd ../
sudo rm -rf bcm2835-1.60*

echo "Installing wiringPi libraries"
mkdir tmp
cd tmp
wget https://project-downloads.drogon.net/wiringpi-latest.deb
sudo dpkg -i wiringpi-latest.deb
gpio -v
cd ../
sudo rm -rf tmp

echo "Installing Python libraries"
sudo apt-get update
sudo apt-get install -y python3-pip
sudo apt-get install -y python3-pil
sudo apt-get install -y python3-numpy
sudo pip3 install RPi.GPIO
sudo pip3 install spidev



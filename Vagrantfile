# -*- mode: ruby -*-
# vi: set ft=ruby :

# vagrant plugin install vagrant-hostmanager

Vagrant.configure(2) do |config|
  config.ssh.insert_key = false
  config.ssh.forward_agent = true
  config.hostmanager.enabled = true
  config.hostmanager.manage_host = true
  config.vm.box = "ubuntu/xenial64"
  config.vm.network "private_network", ip: "192.168.33.10"
  config.vm.hostname = "my.sandbox.local"
  config.vm.synced_folder "./provisioning", "/home/vagrant/provisioning"
  config.vm.provider "virtualbox" do |vb|
    vb.name = "mysandbox"
    vb.cpus = 1
    vb.memory = 2048
  end
  config.vm.provision "shell", inline: <<-SHELL
    set -e
    export DEBIAN_FRONTEND=noninteractive
    sudo apt-get update
    sudo apt-get -y install apt-transport-https ca-certificates curl software-properties-common
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
    sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
    sudo apt update
    sudo apt-get -y install docker-ce
    sudo curl -L https://github.com/docker/compose/releases/download/1.19.0/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    sudo docker-compose -f /home/vagrant/provisioning/docker-compose.yml up -d
  SHELL
end


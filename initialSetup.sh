#!/usr/bin/env bash
set -euo pipefail

mode=${1:-default}
project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

install_base_packages() {
  echo "Installing basic packages"
  sudo apt-get update
  # Install general basic packages
  sudo apt-get -y install unzip curl wget apt-transport-https gpg vim
  # Install basic packages for Cypress
  sudo apt-get -y install libgtk-3-0 libgbm-dev libnotify-dev libnss3 libxss1 libasound2t64 libxtst6 xauth xvfb
}

install_java() {
  echo "Install required Java version"
  wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor | sudo tee /etc/apt/trusted.gpg.d/adoptium.gpg > /dev/null
  echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
  sudo apt-get update
  sudo apt-get -y install temurin-21-jdk
  echo "Setting Java version"
  echo PATH=/usr/lib/jvm/temurin-21-jdk-amd64/bin:$PATH >> ~/.bashrc
  source_bashrc
}

set_automatic_sourcing() {
  echo "Activate automatic sourcing of the .env.dev file"
  echo "set -a" >> ~/.bashrc
  echo "source $project_root/environments/.env.dev"  >> ~/.bashrc
  echo "set +a"  >> ~/.bashrc
  source_bashrc
}

install_node() {
  echo "Install node and npm"
  curl -o- https://fnm.vercel.app/install | bash
  source_bashrc
  fnm install 24
  npm install -g npm
}

update_opencode() {
  echo "Update OpenCode"
   opencode upgrade
}

source_bashrc() {
  set +u
  source ~/.bashrc
  set -u
}

configure_git() {
  echo "Configure git"
  git config --global credential.helper '!f() { if [ "$1" = get ]; then echo username=placeholder-github-user; echo password=placeholder-github-token; fi; }; f'
  echo "Enter user name for git (use your corresponding GitHub user name)"
  read username
  git config user.name "$username"
  echo "Enter email for git (use your no-reply email from GitHub)"
  read email
  git config user.email "$email"
}

initialize_stack() {
  echo "Start stack using full reset and self-signed certificates"
  cd "$project_root"
  ./manageLocalStack.sh --reset
  echo "Waiting to avoid potential race conditions after start-up"
  sleep 15
  echo "Trigger prepopulation of the fake fixture data"
  cd ./dataland-frontend
  npm run prepopulate
}

install_base_packages
install_java
install_node
update_opencode
set_automatic_sourcing
if [[ $mode == "developer" ]]; then
  configure_git
fi
initialize_stack

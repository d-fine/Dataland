#!/usr/bin/env bash
set -euo pipefail

mode=${1:default}

set_java_version() {
  echo PATH=/usr/lib/jvm/temurin-21-jdk-amd64/bin:$PATH >> ~/.bashrc
  source_bashrc
}

set_automatic_sourcing() {
  project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  echo "set -a" >> ~/.bashrc
  echo "source $project_root/environments/.env.dev"  >> ~/.bashrc
  echo "set +a"  >> ~/.bashrc
}

install_node() {
  curl -o- https://fnm.vercel.app/install | bash
  source_bashrc
  fnm install 24
  npm install -g npm
}

update_opencode() {
   opencode upgrade
}

source_bashrc() {
  set +u
  source ~/.bashrc
  set -u
}

configure_git() {
  git config --global credential.helper '!f() { if [ "$1" = get ]; then echo username=placeholder-github-user; echo password=placeholder-github-token; fi; }; f'
  read username
  git config user.name "$username"
  read email
  git config user.email "$email"
}

set_java_version
install_node
update_opencode
set_automatic_sourcing

if [[ $mode == "developer" ]]; then
  configure_git
fi
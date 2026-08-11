#!/usr/bin/env bash
set -euo pipefail

set_java_version() {
  echo PATH=/usr/lib/jvm/temurin-21-jdk-amd64/bin:$PATH >> ~/.bashrc
  source_bashrc
}

set_automatic_sourcing() {
  project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  echo set -a >> ~/.bashrc
  source $project_root/environments/.env.dev  >> ~/.bashrc
  set +a  >> ~/.bashrc
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

set_java_version
install_node
update_opencode
set_automatic_sourcing

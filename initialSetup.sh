#!/usr/bin/env bash
set -euo pipefail

mode=${1:-default}
project_root="$(pwd)"

install_base_packages() {
	echo "Installing basic packages"
	sudo apt-get update
	sudo apt-get -y install unzip curl wget apt-transport-https gpg vim
}

install_java() {
	echo "Install required Java version"
	wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor | sudo tee /etc/apt/trusted.gpg.d/adoptium.gpg >/dev/null
	echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
	sudo apt-get update
	sudo apt-get -y install temurin-21-jdk
	echo "Setting Java version"
	echo PATH=/usr/lib/jvm/temurin-21-jdk-amd64/bin:$PATH >>~/.bashrc
	source_bashrc
}

set_automatic_sourcing() {
	echo "Activate automatic sourcing of the .env.dev file"
	echo "set -a" >>~/.bashrc
	echo "source $project_root/environments/.env.dev" >>~/.bashrc
	echo "set +a" >>~/.bashrc
	source_bashrc
}

install_cypress_prerequisites() {
	echo "Install basic packages for Cypress"
	sudo apt-get -y install libgtk-3-0t64 libgbm-dev libnotify-dev libnss3 libxss1 libasound2t64 libxtst6 xauth xvfb
	echo "Install node and npm"
	curl -o- https://fnm.vercel.app/install | bash
	source_bashrc
	fnm install 24
	npm install -g npm
}

sandbox_cypress_gui_forwarding() {
	echo "Install packages needed to forward the Cypress UI (e.g. 'npm run cypress' in dataland-frontend) to the desktop"
	sudo apt-get -y install x11vnc novnc websockify xauth x11-xserver-utils x11-utils

	local display=":99"

	echo "Persist DISPLAY=$display so future shells (and Cypress) use the virtual display"
	if ! grep -q "^export DISPLAY=$display" ~/.bashrc; then
		echo "export DISPLAY=$display" >>~/.bashrc
	fi
	export DISPLAY="$display"

	echo "Register auto-recovery of Xvfb/x11vnc/noVNC in ~/.bashrc so it survives sandbox restarts"
	if ! grep -q "ensureCypressGuiForwarding.sh" ~/.bashrc; then
		{
			echo "if [ -f \"$project_root/ensureCypressGuiForwarding.sh\" ]; then"
			echo "	bash \"$project_root/ensureCypressGuiForwarding.sh\" || true"
			echo "fi"
		} >>~/.bashrc
	fi

	echo "Start Xvfb/x11vnc/noVNC now"
	bash "$project_root/ensureCypressGuiForwarding.sh"

	echo "Cypress UI will now render on DISPLAY=$display; forward/open port 6080 on your desktop and browse to http://localhost:6080/vnc.html to view it"
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
	git config --global credential.helper '!f() { if [ "$1" = get ]; then echo username=placeholder-github-user; echo password=$GH_TOKEN; fi; }; f'
	echo "Enter user name for git (use your corresponding GitHub user name)"
	read username
	git config user.name "$username"
	echo "Enter email for git (use your no-reply email from GitHub)"
	read email
	git config user.email "$email"
}

initialize_stack() {
	echo "Start stack using full reset"
	echo "(uses SSL certs already present in ./local/certs, e.g. copied via fetch_ssl_certs_for_sandbox.sh; falls back to self-signed certs otherwise)"
	cd "$project_root"
	./manageLocalStack.sh --reset
	echo "Waiting to avoid potential race conditions after start-up"
	sleep 15
	echo "Trigger prepopulation of the fake fixture data"
	cd ./dataland-frontend
	npm run prepopulate
}

install_base_packages
install_cypress_prerequisites
sandbox_cypress_gui_forwarding
install_java
update_opencode
set_automatic_sourcing
if [[ $mode == "developer" ]]; then
	configure_git
fi
initialize_stack

#!/usr/bin/env bash
set -euo pipefail

display=":99"
vnc_port=5900
novnc_port=6080

ensure_x11_socket_dir() {
  if [ ! -d /tmp/.X11-unix ]; then
    sudo mkdir -p /tmp/.X11-unix
    sudo chown root:root /tmp/.X11-unix
    sudo chmod 1777 /tmp/.X11-unix
  fi
}

ensure_xvfb() {
  if ! pgrep -f "Xvfb $display" >/dev/null; then
    nohup Xvfb "$display" -screen 0 1920x1080x24 >/tmp/xvfb.log 2>&1 &
    disown
  fi
  for _ in $(seq 1 20); do
    DISPLAY="$display" xdpyinfo >/dev/null 2>&1 && return 0
    sleep 0.5
  done
  echo "Xvfb did not come up on $display, see /tmp/xvfb.log" >&2
  cat /tmp/xvfb.log >&2 || true
  return 1
}

ensure_x11vnc() {
  if ! pgrep -f "x11vnc -display $display" >/dev/null; then
    # x11vnc wrongly auto-detects Wayland if WAYLAND_DISPLAY/XDG_SESSION_TYPE leak in from the
    # host session, so explicitly unset them and force X11 mode.
    nohup env -u WAYLAND_DISPLAY -u XDG_SESSION_TYPE x11vnc -display "$display" -forever -shared -nopw -rfbport "$vnc_port" >/tmp/x11vnc.log 2>&1 &
    disown
    sleep 1
  fi
  if ! pgrep -f "x11vnc -display $display" >/dev/null; then
    echo "x11vnc failed to start, see /tmp/x11vnc.log" >&2
    cat /tmp/x11vnc.log >&2 || true
    return 1
  fi
}

ensure_novnc() {
  if ! pgrep -f "websockify.*$novnc_port" >/dev/null; then
    # Bind on "[::]" (dual-stack) instead of the IPv4-only default, since sandbox port
    # publishing exposes both IPv4 and IPv6 loopback and some clients (notably Windows/Chrome)
    # prefer connecting via ::1 first.
    nohup websockify --web=/usr/share/novnc/ "[::]:$novnc_port" "localhost:$vnc_port" >/tmp/novnc.log 2>&1 &
    disown
    sleep 1
  fi
  if ! pgrep -f "websockify.*$novnc_port" >/dev/null; then
    echo "websockify failed to start, see /tmp/novnc.log" >&2
    cat /tmp/novnc.log >&2 || true
    return 1
  fi
}

ensure_x11_socket_dir
ensure_xvfb
ensure_x11vnc
ensure_novnc

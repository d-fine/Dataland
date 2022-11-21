function get_container_id_by_service_name () {
  docker_container_service_map=$(docker inspect --format='{{.Id}} "{{index .Config.Labels "com.docker.compose.service"}}";' $(docker ps -aq))
  echo $(echo $docker_container_service_map | tr ";" "\n" | grep "\"$1\"" | awk '{print $1}')
}

function require_services_healthy () {
  docker_healthcheck=$(docker inspect --format='"{{index .Config.Labels "com.docker.compose.service"}}";{{ if .State.Health}}{{.State.Health.Status}}{{else}}unknown{{end}}' $(docker ps -aq))
  for service in "$@"
  do
    if [[ $docker_healthcheck != *"\"$service\";healthy"* ]]; then
      echo "Service $service not healthy:"
      docker inspect --format='{{json .State.Health}}' $(get_container_id_by_service_name $service)
    fi
  done
}

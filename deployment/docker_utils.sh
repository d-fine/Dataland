function get_container_id_by_service_name () {
  docker_container_service_map=$(sudo docker inspect --format='{{.Id}} "{{index .Config.Labels "com.docker.compose.service"}}";' $(docker ps -aq))
  echo $(echo $docker_container_service_map | tr ";" "\n" | grep "\"$1\"" | awk '{print $1}')
}
export -f get_container_id_by_service_name

function get_services_that_are_not_healthy_from_list () {
  docker_healthcheck=$(sudo docker inspect --format='"{{index .Config.Labels "com.docker.compose.service"}}";{{ if .State.Health}}{{.State.Health.Status}}{{else}}unknown{{end}}' $(docker ps -aq))
  for service in "$@"
  do
    if [[ $docker_healthcheck != *"\"$service\";healthy"* ]]; then
      echo "Service $service not healthy"
      sudo docker inspect --format='{{json .State.Health}}' $(get_container_id_by_service_name $service)
    fi
  done
}
export -f get_services_that_are_not_healthy_from_list

function get_services_that_are_not_healthy_but_should_be_in_compose_profile () {
  service_list=$(get_services_in_docker_compose_profile_that_require_healthcheck $1)
  get_services_that_are_not_healthy_from_list $service_list
}
export -f get_services_that_are_not_healthy_but_should_be_in_compose_profile

function get_services_in_docker_compose_profile_that_require_healthcheck () {
  sudo docker compose --profile $1 config --services 2>/dev/null | grep -vx "edc-dummyserver\|dataland-edc\|e2etests"
}
export -f get_services_in_docker_compose_profile_that_require_healthcheck

function wait_for_services_healthy_in_compose_profile () {
  while true; do
    health_check_results=$(get_services_that_are_not_healthy_but_should_be_in_compose_profile $1)
    if [ -z "$health_check_results" ]; then
      echo "All relevant containers are healthy!"
      break
    else
      echo "Some containers are NOT HEALTHY. Retrying in 1 second."
      echo "$health_check_results"
    fi
    sleep 1
  done
}
export -f wait_for_services_healthy_in_compose_profile

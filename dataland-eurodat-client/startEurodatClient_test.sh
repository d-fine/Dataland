docker run -p 12345:8443 \
  -v //c/Users/d92048/IdeaProjects/Dataland5/dataland-eurodat-client/crt/tls.crt:/crt/tls.crt \
  -v //c/Users/d92048/IdeaProjects/Dataland5/dataland-eurodat-client/keystore.jks:/crt/keystore.jks \
  -v //c/Users/d92048/IdeaProjects/Dataland5/dataland-eurodat-client/test.jks:/crt/test.jks \
  --env-file ./client.env \
  registry.gitlab.com/eurodat/trustee-platform/client-controller:v0.0.28

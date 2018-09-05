# Teamcity Netease (163yun) Cloud Agent

Run teamcity agents on 163yun container service.

## Compatibility

Tested on teamcity 2018.1.x.

163yun: currently only support cn-east-1b zone (in vpc).

## Configuration

Install plugin and

* access key
* access secret
* machine type (1c2g, 2c4g, 4c8g)
* agent image (official teamcity agent docker image, shared from *patest* account)
  https://c.163yun.com/hub#/m/repository/?repoId=87523
* namespace
* vpc
* subnet
* security group
* (optional) server url

## Agent Env

* docker_in_docker mode
* A high performance cloud disk is created and reused, mounted to /var/lib/docker/. It will not delete after agent destroy.
* cloud disk will cache docker images, yarn cache, gradle home and teamcity agent working directory.
* with following envs:
  * SERVER_URL (for agent official image)
  * DOCKER_IN_DOCKER: start (for docker in docker support)
  * ENV_NETEASE_TC_AGENT: true
  * ENV_INSTANCE_ID (an internal agent id)
  * YARN_CACHE_FOLDER: /var/lib/docker/yarn/
  * GRADLE_USER_HOME: /var/lib/docker/gradle/
  * AGENT_OPTS: workDir=/var/lib/docker/tc/work/ tempDir=/var/lib/docker/tc/temp/ toolsDir=/var/lib/docker/tc/tools/ pluginsDir=/var/lib/docker/tc/plugins/ systemDir=/var/lib/docker/tc/system/

## Limitation

* Currently java SDK is not provided by 163yun. A jython runtime is included in plugin for api. This will be deleted once java SDK is provided.

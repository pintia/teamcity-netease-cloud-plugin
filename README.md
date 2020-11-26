# TeamCity Netease Cloud (163yun) Agent

Run TeamCity agents on Netease Cloud container service.

## Compatibility

Tested on TeamCity 2018.1, 2018.2, 2019.2.

Netease Cloud: Only support cn-east-1b zone (in VPC).

## Configuration

Install the plugin and change settings:

* Access Key
* Access Secret
* Machine Type (1c2g, 2c4g, 4c8g)
* Agent image repo and tag (in your netease cloud account's image repo)
* Create disk automatically option and disk size
* Namespace
* VPC
* Subnet
* Security Group

## Agent Environment Variables

* docker_in_docker mode
* A high performance cloud disk will/should be created and reused, mounted to /var/lib/docker/. It will not be deleted after agent destroy.
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

* Currently java SDK is not provided by Netease Cloud. A jython runtime is included in the plugin. This will be removed once official Netease Cloud java SDK provided.

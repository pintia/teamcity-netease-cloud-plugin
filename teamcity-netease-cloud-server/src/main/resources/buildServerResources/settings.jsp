<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/include.jsp" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="agentPools" scope="request" type="java.util.Collection<jetbrains.buildServer.serverSide.agentPools.AgentPool>"/>
<jsp:useBean id="constants" class="com.hlyue.teamcity.agent.netease.Constants"/>
<jsp:useBean id="basePath" class="java.lang.String" scope="request"/>

<tr>
    <th><label for="${constants.PREFERENCE_MACHINE_TYPE}">Machine Type: <l:star/></label></th>
    <td>
        <select name="${constants.PREFERENCE_MACHINE_TYPE}" class="longField" >
        </select>
    </td>
</tr>
<tr>
    <th><label for="prop:${constants.PREFERENCE_CREATE_DISK}_disp">Create disk: </label></th>
    <td>
        <div>
            <input name="prop:${constants.PREFERENCE_CREATE_DISK}_disp" type="checkbox" />
            <input name="prop:${constants.PREFERENCE_CREATE_DISK}" type="hidden" />
        </div>
    </td>
</tr>
<tr>
    <th><label for="prop:${constants.PREFERENCE_DISK_SIZE}">Disk Size (GB): <l:star/></label></th>
    <td>
        <div>
            <input name="prop:${constants.PREFERENCE_DISK_SIZE}" class="longField"
                   value="${propertiesBean.properties[constants.PREFERENCE_DISK_SIZE]}" />
        </div>
    </td>
</tr>
<tr>
    <th><label for="prop:${constants.PREFERENCE_ACCESS_KEY}">Access key: <l:star/></label></th>
    <td>
        <div>
            <input name="prop:${constants.PREFERENCE_ACCESS_KEY}" class="longField"
                   value="${propertiesBean.properties[constants.PREFERENCE_ACCESS_KEY]}" />
        </div>
    </td>
</tr>
<tr>
    <th><label for="prop:${constants.PREFERENCE_ACCESS_SECRET}">Access secret: <l:star/></label></th>
    <td>
        <div>
            <input name="prop:${constants.PREFERENCE_ACCESS_SECRET}" class="longField"
                   value="${propertiesBean.properties[constants.PREFERENCE_ACCESS_SECRET]}" />
        </div>
    </td>
</tr>
<tr>
    <th><label for="${constants.PREFERENCE_REPOSITORY_ID}">Agent image: <l:star/></label></th>
    <td>
        <select name="${constants.PREFERENCE_REPOSITORY_ID}" class="longField" >
        </select>
    </td>
</tr>
<tr>
    <th><label for="${constants.PREFERENCE_IMAGE_TAG}">Agent image tag: <l:star/></label></th>
    <td>
        <select name="${constants.PREFERENCE_IMAGE_TAG}" class="longField" >
        </select>
        <input name="prop:${constants.PREFERENCE_IMAGE_FULL_TAG}" type="hidden" />
    </td>
</tr>
<tr>
    <th><label for="${constants.PREFERENCE_NAMESPACE}">Namespace: <l:star/></label></th>
    <td>
        <select name="${constants.PREFERENCE_NAMESPACE}" class="longField" >
        </select>
    </td>
</tr>
<tr>
    <th><label for="${constants.PREFERENCE_VPC}">Vpc: <l:star/></label></th>
    <td>
        <select name="${constants.PREFERENCE_VPC}" class="longField" >
        </select>
    </td>
</tr>
<tr>
    <th><label for="${constants.PREFERENCE_SUBNET}">Subnet: <l:star/></label></th>
    <td>
        <select name="${constants.PREFERENCE_SUBNET}" class="longField" >
        </select>
    </td>
</tr>
<tr>
    <th><label for="${constants.PREFERENCE_SECURITY_GROUP}">Security Group: <l:star/></label></th>
    <td>
        <select name="${constants.PREFERENCE_SECURITY_GROUP}" class="longField" >
        </select>
    </td>
</tr>
<tr>
    <th><label for="prop:${constants.PREFERENCE_AGENT_POOL}">Agent pool:<l:star/></label></th>
    <td>
        <select name="prop:${constants.PREFERENCE_AGENT_POOL}" class="longField">
            <props:option value=""><c:out value="<Select agent pool>"/></props:option>
            <c:forEach var="ap" items="${agentPools}">
                <props:option selected="${ap.agentPoolId eq propertiesBean.properties[constants.PREFERENCE_AGENT_POOL]}" value="${ap.agentPoolId}"><c:out value="${ap.name}"/></props:option>
            </c:forEach>
        </select>
    </td>
</tr>
<script>
    let accessKeyName = "${constants.PREFERENCE_ACCESS_KEY}"
    let accessSecretName = "${constants.PREFERENCE_ACCESS_SECRET}"
    let namespaceName = "${constants.PREFERENCE_NAMESPACE}"
    let vpcName = "${constants.PREFERENCE_VPC}"
    let subnetName = "${constants.PREFERENCE_SUBNET}"
    let securityGroupName = "${constants.PREFERENCE_SECURITY_GROUP}"
    let machineTypeName = "${constants.PREFERENCE_MACHINE_TYPE}"
    let repositoryIdName = "${constants.PREFERENCE_REPOSITORY_ID}"
    let imageTagName = "${constants.PREFERENCE_IMAGE_TAG}"

    let accessKey = "${propertiesBean.properties[constants.PREFERENCE_ACCESS_KEY]}"
    let accessSecret = "${propertiesBean.properties[constants.PREFERENCE_ACCESS_SECRET]}"
    let namespace = "${propertiesBean.properties[constants.PREFERENCE_NAMESPACE]}"
    let vpc = "${propertiesBean.properties[constants.PREFERENCE_VPC]}"
    let subnet = "${propertiesBean.properties[constants.PREFERENCE_SUBNET]}"
    let securityGroup = "${propertiesBean.properties[constants.PREFERENCE_SECURITY_GROUP]}"
    let machineType = "${propertiesBean.properties[constants.PREFERENCE_MACHINE_TYPE]}"
    let createDisk = "${propertiesBean.properties[constants.PREFERENCE_CREATE_DISK]}" || "0"
    let diskSize = "${propertiesBean.properties[constants.PREFERENCE_DISK_SIZE]}"
    let repositoryId = "${propertiesBean.properties[constants.PREFERENCE_REPOSITORY_ID]}"
    let imageTag = "${propertiesBean.properties[constants.PREFERENCE_IMAGE_TAG]}"
    let imageFullTag = "${propertiesBean.properties[constants.PREFERENCE_IMAGE_FULL_TAG]}"
    let agentPool = $j("input[name='prop:${constants.PREFERENCE_AGENT_POOL}']").val()

    let optionDefault = {
        text: '',
        value: 0,
    }
    let options = [optionDefault]
    let namespaceOptions = options.slice(0)
    let vpcOptions = options.slice(0)
    let subnetOptions = options.slice(0)
    let securityGroupOptions = options.slice(0)
    let repositoryOptions = options.slice(0)
    let imageTagOptions = options.slice(0)
    let machineTypeOptions = options.slice(0)
    <c:forEach items="${constants.MACHINE_TYPE_LIST}" var="item">
        machineTypeOptions.push({
            text: "${item}",
            value: "${item}"
        })
    </c:forEach>

    function getSelectDom(name) {
        return $j('select[name="' + name + '"]')
    }
    function bindSelect(name, options, value) {
        let select = getSelectDom(name).empty()
        options.forEach((item) => {
            select.append($j('<option>', {
                value: item.value,
                text: item.text
            }))
        })
        select.val(value)
        if (select.find('option').length <= 1) {
            select.prop('disabled', true)
        } else {
            select.prop('disabled', false)
        }
        console.log(select.parent())
        select.parent().find('.hidden-select').remove(0)
        select.parent().append($j('<input>', {
            type: 'hidden',
            value: value,
            name: 'prop:' + name
        }).addClass('hidden-select'))
    }
    function debug() {
        console.log('debug:', accessKey, accessSecret, namespace, vpc, subnet, securityGroup)
    }

    function doBind() {
        $j("input[name='prop:${constants.PREFERENCE_ACCESS_KEY}']").val(accessKey)
        $j("input[name='prop:${constants.PREFERENCE_ACCESS_SECRET}']").val(accessSecret)
        $j("input[name='prop:${constants.PREFERENCE_CREATE_DISK}_disp']").get(0).checked = parseInt(createDisk)
        $j("input[name='prop:${constants.PREFERENCE_CREATE_DISK}']").val(createDisk)
        $j("input[name='prop:${constants.PREFERENCE_DISK_SIZE}']").val(diskSize)
        $j("input[name='prop:${constants.PREFERENCE_IMAGE_FULL_TAG}']").val(imageFullTag)
        bindSelect(namespaceName, namespaceOptions, namespace)
        bindSelect(vpcName, vpcOptions, vpc)
        bindSelect(subnetName, subnetOptions, subnet)
        bindSelect(securityGroupName, securityGroupOptions, securityGroup)
        bindSelect(machineTypeName, machineTypeOptions, machineType)
        bindSelect(repositoryIdName, repositoryOptions, repositoryId)
        bindSelect(imageTagName, imageTagOptions, imageTag)
    }

    function post(data, callback) {
        $j.ajax({
            url: "<c:url value='${basePath}'/>",
            data: JSON.stringify(data),
            contentType: 'application/json; charset=utf-8',
            type: 'POST',
            success: callback,
            dataType: 'json'
        })
    }

    function load(resource, callback, params = {}) {
        let data = {
            accessKey: accessKey,
            accessSecret: accessSecret,
            resource: resource,
            params: params
        }
        post(data, callback)
    }

    function loadAndSetOptions(optionsArray, resource, params, dataNameInResponse, mapper = (item) => ({
        value: item.Id,
        text: item.Name
    })) {
        load(resource, (data) => {
            let arr = data[dataNameInResponse]
            optionsArray.splice(1, optionsArray.length - 1)
            arr.forEach((item) => optionsArray.push(mapper.call(this, item)))
            doBind()
        }, params)
    }

    function loadSubnet() {
        loadAndSetOptions(subnetOptions, 'subnet', {
            VpcId: vpc
        }, 'Subnets')
    }

    function loadNamespace() {
        loadAndSetOptions(namespaceOptions, 'namespace', {}, 'Namespaces', (item) => ({
            value: item.NamespaceId,
            text: item.Name
        }))
    }

    function loadVpc() {
        loadAndSetOptions(vpcOptions, 'vpc', {}, 'Vpcs')
    }

    function loadSecurityGroup() {
        loadAndSetOptions(securityGroupOptions, 'securityGroup', {
            VpcId: vpc
        }, 'SecurityGroups')
    }

    function loadRepository() {
        loadAndSetOptions(repositoryOptions, 'repositories', {}, 'Repositories', (item) => ({
            value: item.RepositoryId,
            text: item.Name
        }))
    }

    function loadImageTag() {
        loadAndSetOptions(imageTagOptions, 'repoTags', {
            RepositoryId: repositoryId
        }, 'Tags', (item) => ({
            value: item.Tag,
            text: item.Tag,
            full: item.TagUrl
        }))
    }

    function loadAll() {
        loadSubnet()
        loadNamespace()
        loadVpc()
        loadSecurityGroup()
        loadRepository()
        loadImageTag()
    }

    (() => {
        function ready() {
            doBind()
            loadAll()
            getSelectDom(namespaceName).change(function() {
                namespace = $j(this).find('option:selected').val()
                doBind()
            })
            getSelectDom(vpcName).change(function() {
                vpc = $j(this).find('option:selected').val()
                doBind()
                loadSubnet()
                loadSecurityGroup()
            })
            getSelectDom(subnetName).change(function() {
                subnet = $j(this).find('option:selected').val()
                doBind()
            })
            getSelectDom(securityGroupName).change(function() {
                securityGroup = $j(this).find('option:selected').val()
                doBind()
            })
            getSelectDom(machineTypeName).change(function() {
                machineType = $j(this).find('option:selected').val()
                doBind()
            })
            getSelectDom(repositoryIdName).change(function() {
                repositoryId = $j(this).find('option:selected').val()
                loadImageTag()
                doBind()
            })
            getSelectDom(imageTagName).change(function() {
                imageTag = $j(this).find('option:selected').val()
                imageFullTag = imageTagOptions.find(it => it.value === imageTag).full
                doBind()
            })
            $j("input[name='prop:${constants.PREFERENCE_CREATE_DISK}_disp']").change(function() {
                createDisk = this.checked ? 1 : 0
                doBind()
            })
            $j("input[name='prop:${constants.PREFERENCE_DISK_SIZE}']").change(function() {
                diskSize = this.value
                doBind()
            })
            $j("input[name='prop:${constants.PREFERENCE_ACCESS_KEY}']").change(function() {
                accessKey = this.value
                doBind()
                loadAll()
            })
            $j("input[name='prop:${constants.PREFERENCE_ACCESS_SECRET}']").change(function() {
                accessSecret = this.value
                doBind()
                loadAll()
            })
            $j("input[name='prop:${constants.PREFERENCE_AGENT_POOL}']").change(function() {
                agentPool = this.value
            })
        }
        $j(document).ready(ready)
    })()
</script>

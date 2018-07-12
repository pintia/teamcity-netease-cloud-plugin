<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/include.jsp" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="constants" class="com.hlyue.teamcity.agent.netease.Constants"/>
<jsp:useBean id="basePath" class="java.lang.String" scope="request"/>

<l:settingsGroup title="Security Credentials">
    <tr>
        <th><label for="${constants.PREFERENCE_ACCESS_KEY}">Access key: <l:star/></label></th>
        <td>
            <div>
                <input name="prop:${constants.PREFERENCE_ACCESS_KEY}" class="longField"
                       value="<c:out value="${propertiesBean.properties[constants.PREFERENCE_ACCESS_KEY]}"/>" />
            </div>
        </td>
    </tr>
    <tr>
        <th><label for="${constants.PREFERENCE_ACCESS_SECRET}">Access secret: <l:star/></label></th>
        <td>
            <div>
                <input name="prop:${constants.PREFERENCE_ACCESS_SECRET}" class="longField"
                       data-bind="initializeValue: ${propertiesBean.properties[constants.PREFERENCE_ACCESS_SECRET]}"
                       value="<c:out value="${propertiesBean.properties[constants.PREFERENCE_ACCESS_SECRET]}"/>" />
            </div>
        </td>
    </tr>
    <tr>
        <th><label for="${constants.PREFERENCE_MACHINE_TYPE}_SELECT">Machine Type: <l:star/></label></th>
        <td>
            <select name="${constants.PREFERENCE_MACHINE_TYPE}_SELECT" class="longField"
                    onchange="coorChange.call(this, 'input[name=\'prop:${constants.PREFERENCE_MACHINE_TYPE}\']')">
                <c:forEach items="${constants.MACHINE_TYPE_LIST}" var="item">
                    <option value="${item}"
                        <c:if test="${item==propertiesBean.properties[constants.PREFERENCE_MACHINE_TYPE]}">selected</c:if> >
                        ${item}
                    </option>
                </c:forEach>
            </select>
            <input style="display: none;" name="prop:${constants.PREFERENCE_MACHINE_TYPE}"  class="longField"
                   value="<c:out value="${propertiesBean.properties[constants.PREFERENCE_MACHINE_TYPE]}"/>" />
        </td>
    </tr>
    <tr>
        <th><label for="${constants.PREFERENCE_NAMESPACE}_SELECT">Namespace: <l:star/></label></th>
        <td>
            <select name="${constants.PREFERENCE_NAMESPACE}_SELECT" class="longField"
                    onchange="coorChange.call(this, 'input[name=\'prop:${constants.PREFERENCE_NAMESPACE}\']')">
            </select>
            <input style="display: none;" name="prop:${constants.PREFERENCE_NAMESPACE}"  class="longField"
                   value="<c:out value="${propertiesBean.properties[constants.PREFERENCE_NAMESPACE]}"/>" />
        </td>
    </tr>
    <tr>
        <th><label for="${constants.PREFERENCE_VPC}_SELECT">Vpc: <l:star/></label></th>
        <td>
            <select name="${constants.PREFERENCE_VPC}_SELECT" class="longField"
                    onchange="coorChange.call(this, 'input[name=\'prop:${constants.PREFERENCE_VPC}\']', windowLoadSubnet)">
            </select>
            <input style="display: none;" name="prop:${constants.PREFERENCE_VPC}"  class="longField"
                   value="<c:out value="${propertiesBean.properties[constants.PREFERENCE_VPC]}"/>" />
        </td>
    </tr>
    <tr>
        <th><label for="${constants.PREFERENCE_SUBNET}_SELECT">Subnet: <l:star/></label></th>
        <td>
            <select name="${constants.PREFERENCE_SUBNET}_SELECT" class="longField"
                    onchange="coorChange.call(this, 'input[name=\'prop:${constants.PREFERENCE_SUBNET}\']')">
            </select>
            <input style="display: none;" name="prop:${constants.PREFERENCE_SUBNET}"  class="longField"
                   value="<c:out value="${propertiesBean.properties[constants.PREFERENCE_SUBNET]}"/>" />
        </td>
    </tr>
    <tr>
        <th><label for="${constants.PREFERENCE_SECURITY_GROUP}_SELECT">Security Group: <l:star/></label></th>
        <td>
            <select name="${constants.PREFERENCE_SECURITY_GROUP}_SELECT" class="longField"
                    onchange="coorChange.call(this, 'input[name=\'prop:${constants.PREFERENCE_SECURITY_GROUP}\']')">
            </select>
            <input style="display: none;" name="prop:${constants.PREFERENCE_SECURITY_GROUP}"  class="longField"
                   value="<c:out value="${propertiesBean.properties[constants.PREFERENCE_SECURITY_GROUP]}"/>" />
        </td>
    </tr>
    <script type="application/javascript">
        let windowLoadSubnet
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
        function coorChange(input, callback) {
            let value = this.options[this.options.selectedIndex].value
            $j(input).val(value)
            if (callback) callback.call(this, value)
        }

        function ready() {
            let keyDom = $j("input[name='prop:${constants.PREFERENCE_ACCESS_KEY}']")
            let secretDom = $j("input[name='prop:${constants.PREFERENCE_ACCESS_SECRET}']")
            let load = (resource, callback, params = {}) => {
                console.log(params)
                let data = {
                    accessKey: keyDom.val(),
                    accessSecret: secretDom.val(),
                    resource: resource,
                    params: params
                }
                console.log(data)
                post(data, callback)
            }

            let loadSubnet = (vpcId) => load('subnet', (data) => {
                const select = $j("select[name='${constants.PREFERENCE_SUBNET}_SELECT']").empty()
                data.Subnets.forEach((item) => {
                    select.append($j('<option>', {
                        value: item.Id,
                        text: item.Name,
                        selected: item.Id == "${propertiesBean.properties[constants.PREFERENCE_SUBNET]}"
                    }))
                })
            }, {
                VpcId: vpcId || $j('input[name=\'prop:${constants.PREFERENCE_VPC}\']').val()
            })

            let loadSecurityGroup = (vpcId) => load('securityGroup', (data) => {
                const select = $j("select[name='${constants.PREFERENCE_SECURITY_GROUP}_SELECT']").empty()
                data.SecurityGroups.forEach((item) => {
                    select.append($j('<option>', {
                        value: item.Id,
                        text: item.Name,
                        selected: item.Id == "${propertiesBean.properties[constants.PREFERENCE_SECURITY_GROUP]}"
                    }))
                })
            }, {
                VpcId: vpcId || $j('input[name=\'prop:${constants.PREFERENCE_VPC}\']').val()
            })

            windowLoadSubnet = (vpcId) => {
                loadSubnet(vpcId)
                loadSecurityGroup(vpcId)
            }

            let loadNamespaces = () => load('namespace', (data) => {
                const select = $j("select[name='${constants.PREFERENCE_NAMESPACE}_SELECT']").empty()
                data.Namespaces.forEach((item) => {
                    select.append($j('<option>', {
                        value: item.NamespaceId,
                        text: item.Name,
                        selected: item.NamespaceId == "${propertiesBean.properties[constants.PREFERENCE_NAMESPACE]}"
                    }))
                })
            })

            let loadVpc = () => load('vpc', (data) => {
                const select = $j("select[name='${constants.PREFERENCE_VPC}_SELECT']").empty()
                data.Vpcs.forEach((item) => {
                    select.append($j('<option>', {
                        value: item.Id,
                        text: item.Name,
                        selected: item.Id == "${propertiesBean.properties[constants.PREFERENCE_VPC]}"
                    }))
                })
            })

            let loadAll = () => {
                loadNamespaces()
                loadVpc()
                loadSubnet()
                loadSecurityGroup()
            }

            keyDom.change(loadAll)
            secretDom.change(loadAll)
            loadAll()
        }
        $j(document).ready(ready)
    </script>
</l:settingsGroup>

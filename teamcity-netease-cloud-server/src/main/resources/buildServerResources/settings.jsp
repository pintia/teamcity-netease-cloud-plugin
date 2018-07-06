<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/include.jsp" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="constants" class="com.hlyue.teamcity.agent.netease.Constants"/>

<l:settingsGroup title="Security Credentials">
    <tr>
        <th><label for="${constants.PREFERENCE_ACCESS_KEY}">Access key: <l:star/></label></th>
        <td>
            <div>
                <input name="prop:${constants.PREFERENCE_ACCESS_KEY}" class="longField"
                       data-bind="initializeValue: propertiesBean.properties[constants.PREFERENCE_ACCESS_KEY]"
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
                    onchange="$j('input[name=\'prop:${constants.PREFERENCE_MACHINE_TYPE}\']').val(this.options[this.options.selectedIndex].value)">
                <c:forEach items="${constants.MACHINE_TYPE_LIST}" var="item">
                    <option value="${item}"
                        <c:if test="${item==propertiesBean.properties[constants.PREFERENCE_MACHINE_TYPE]}">selected</c:if> >
                        ${item}
                    </option>
                </c:forEach>
            </select>
            <input style="display: none;" name="prop:${constants.PREFERENCE_MACHINE_TYPE}"  class="longField"
                   data-bind="initializeValue: ${propertiesBean.properties[constants.PREFERENCE_MACHINE_TYPE]}"
                   value="<c:out value="${propertiesBean.properties[constants.PREFERENCE_MACHINE_TYPE]}"/>" />
        </td>
    </tr>
</l:settingsGroup>

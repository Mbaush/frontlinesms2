<%@ page import="frontlinesms2.WebConnection" %>
<g:if test="${!activityInstanceToEdit}">
	<div class="input">
		<label for="pollType"><g:message code="connection.select"/></label>
		<ul class="select">
			<g:each in="${WebConnection.implementations}" status="i" var="it">
				<li>
					<label for="connectionType"><g:message code="${it.simpleName.toLowerCase()}.label"/></label>
					<g:radio name="connectionType" checked="${i == 0}"
							value="${it.shortName}" onclick="fconnection.setType('${it.shortName}')"/>
				</li>
			</g:each>
		</ul>
	</div>
</g:if>
<g:else>
	<g:hiddenField name="connectionType" value="${fconnectionInstance?.shortName}"/>
</g:else>


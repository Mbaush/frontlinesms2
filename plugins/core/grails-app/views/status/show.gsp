<%@ page contentType="text/html;charset=UTF-8" %>
<html>
	<head>
		<meta name="layout" content="status_layout" />
	</head>
	<body>
		<g:render template="traffic" />
		<div id="right-column">
			<g:render template="connection_list" />
			<h3 id="detection-title">Detected devices</h3>
			<g:render template="device_detection"/>
		</div>
		<g:javascript>
			setInterval(refreshDevices, 10000);
			
			function refreshDevices() {
				$.get(url_root + 'status/listDetected', function(data) {
					$('#device-detection').replaceWith($(data));
				});
			}
		</g:javascript>
	</body>
</html>
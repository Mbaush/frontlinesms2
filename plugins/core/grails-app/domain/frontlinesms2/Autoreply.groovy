package frontlinesms2

class Autoreply extends Activity {
	def messageSendService
	static hasOne = [keyword: Keyword]
	String autoreplyText
	
	static constraints = {
		name(blank: false, nullable: false, maxSize: 255, unique: true)
		autoreplyText(nullable:false, blank:false)
	}
	
	static mapping = {
		keyword cascade: 'all'
	}
	
	def getType() {
		return 'autoreply'
	}

	def processKeyword(Fmessage message, boolean exactMatch) {
		if(!exactMatch && keyword.value) return
		def autoreply = this
		def params = [:]
		params.addresses = message.src
		params.messageText = autoreply.autoreplyText
		autoreply.addToMessages(message)
		def outgoingMessage = messageSendService.createOutgoingMessage(params)
		autoreply.addToMessages(outgoingMessage)
		messageSendService.send(outgoingMessage)
		autoreply.save()
		println "Autoreply message sent to ${message.src}"
	}
}


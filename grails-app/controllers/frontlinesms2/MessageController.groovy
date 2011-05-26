package frontlinesms2

class MessageController {
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index = {
		redirect(action: "inbox", params: params)
	}

	def show = {
		def messageInstance = Fmessage.get(params.id)
		def pollInstance = Poll.get(params.pollId)
		def contactInstance
		if(!messageInstance.read) {
			messageInstance.read = true
			messageInstance.save()
		}

		if(messageInstance) {
			contactInstance = Contact.findByAddress(messageInstance.src)
		}

		render view:params.messageSection,
				model:[messageInstance: messageInstance,
						contactInstance: contactInstance,
						pollInstanceList: Poll.findAll(),
						pollInstance: pollInstance] << "${params.messageSection}"()
	}

    def inbox = {
		params.sort = 'dateCreated'
		params.order = 'desc'
		params.inbound = true
		[messageSection:'inbox',
			messageInstanceList: Fmessage.getInboxMessages(),
			messageInstanceTotal: Fmessage.getInboxMessages().size()] << list()
    }

    def sent = {
		params.inbound = false
		[messageSection:'sent'] << list()
    }

	def poll = {
		def pollInstance = Poll.get(params.pollId)
		
		[messageSection:'poll',
				messageInstanceList: pollInstance.messages,
				messageInstanceTotal: pollInstance.messages.size(),
				pollInstanceList: Poll.findAll(),
				pollInstance: pollInstance,
				pollResponseList: pollInstance.responses]
	}

	def list = {
		[pollInstanceList: Poll.findAll()]
	}
	
	def move = {
		def pollInstance = Poll.get(params.pollId)
		def messageInstance = Fmessage.get(params.id)
		pollInstance.responses.toArray()[0].addToMessages(messageInstance).save(failOnError: true, flush: true)
		redirect(action: "show", params: params)
	}
}

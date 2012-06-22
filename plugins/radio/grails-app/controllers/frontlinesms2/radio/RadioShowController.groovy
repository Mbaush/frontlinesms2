package frontlinesms2.radio

import frontlinesms2.MessageController
import frontlinesms2.*
import java.util.Date
import grails.converters.*
import java.text.SimpleDateFormat

class RadioShowController extends MessageController {
	static allowedMethods = [save: "POST"]
	
	def index = {
		params.sort = 'date'
		redirect(action:messageSection, params:params)
	}
	
	def create = {
		[showInstance: new RadioShow()]
	}

	def save = {	
		def showInstance = new RadioShow()
		showInstance.properties = params
		if (showInstance.validate()) {
			showInstance.save()
			flash.message = message(code: 'radio.show.created')
		} else {
			flash.message = message(code: 'radio.show.invalid.name')
		}
		redirect(controller: 'message', action: "inbox")
	}
	
	def radioShow() {
		def showInstance = RadioShow.get(params.ownerId)
		if(showInstance) {
				def messageInstanceList = showInstance?.getShowMessages(params.starred)
				def radioMessageInstanceList = []
				messageInstanceList?.list(params).inject([]) { messageB, messageA ->
				    if(messageB && dateToString(messageB.date) != dateToString(messageA.date) && params.sort == 'date')
				        radioMessageInstanceList.add(dateToString(messageA.date))
				    radioMessageInstanceList.add(messageA)
				    return messageA
				}
				render view:'standard',
					model:[messageInstanceList: radioMessageInstanceList,
						   messageSection: 'radioShow',
						   messageInstanceTotal: messageInstanceList?.count(),
						   ownerInstance: showInstance] << this.getShowModel()
		} else {
			flash.message = message(code: 'radio.show.not.found')
			redirect(action: 'inbox')
		}
	}
	
	def startShow = {
		def showInstance = RadioShow.findById(params.id)
		println "params.id: ${params.id}"
		if(showInstance?.start()) {
			println "${showInstance.name} show started"
			showInstance.save(flush:true)
			render "$showInstance.id"
		} else {
			flash.message = message code:'radio.show.onair.error', args:[RadioShow.findByIsRunning(true)?.name]
			render text:flash.message
		}
	}
	
	def stopShow = {
		def showInstance = RadioShow.findById(params.id)
		showInstance.stop()
		showInstance.save(flush:true)
		render "$showInstance.id"
	}
	
	def getShowModel(messageInstanceList) {
		def model = super.getShowModel(messageInstanceList)
		model << [radioShowInstanceList: RadioShow.findAll()]
		return model
	}
	
	def getNewRadioMessageCount = {
		if(params.messageSection == 'radioShow') {
			def messageCount = [totalMessages:[RadioShow.get(params.ownerId)?.getShowMessages()?.count()]]
			render messageCount as JSON
		} else {
			getNewMessageCount()
		}
	}
	
	def addActivity = {
		def activityInstance = Activity.get(params.activityId)
		def showInstance = RadioShow.get(params.radioShowId)
		
		if(showInstance && activityInstance) {
			showInstance.addToActivities(activityInstance)
		}
		redirect controller:"message", action:"activity", params: [ownerId: params.activityId]
	}
	
	def selectActivity = {
		def activityInstance = Activity.get(params.ownerId)
		[ownerInstance:activityInstance]
	}
	
	private void removeActivityFromRadioShow(Activity activity) {
		RadioShow.findAll().collect { showInstance ->
			if(activity in showInstance.activities) {
				showInstance.removeFromActivities(activity)
				showInstance.save()
			}
		}
	}
	
	private String dateToString(Date date) {
		new SimpleDateFormat("EEEE, MMMM dd", Locale.US).format(date)
	}
	
}

package frontlinesms2.contact

import frontlinesms2.*
import geb.navigator.EmptyNavigator

class ContactAddGroupSpec extends ContactBaseSpec {

	def setup() {
		createTestContacts()
		createTestGroups()
	}
	
	def 'groups that selected contact belongs to are shown in contact details'() {
		when:
			to PageContactShowBob
		then:
			def memberOf = $("#group-list li").children('span')*.text().sort()
			memberOf == ['Test', 'three']
	}
	
	def 'existing groups that contact is not a member of can be selected from dropdown and are then added to list'() {
		when:
			to PageContactShowBob
		then:
			$("#group-dropdown").children('option')*.text().sort() == ['Add to group...', 'Others', 'four']
		when:
			$("#group-dropdown").value("${Group.findByName('Others').id}")
		then:
			waitFor { $("#group-list li span")*.text().sort() == ['Others', 'Test', 'three'] }
			$("#group-dropdown").children()*.text() == ['Add to group...', 'four']
			
	}

	def 'clicking X next to group in list removes group from visible list, but does not change database iff no other action is taken'() {
		given:
			def bob = Contact.findByName("Bob")
			def bobsDatabaseGroups = bob.groups
			def bobsGroups = bobsDatabaseGroups
		when:
			to PageContactShowBob
		then:
			groupList.find('li span').size() == 2
			def groupsText = groupList.find('li span')*.text()
			groupsText.containsAll(['Test', 'three'])
		when:
			groupList.find('a').first().click()
			bobsGroups = bob.groups
		then:
			waitFor { groupList.find('li span').size() == 1 }
		when:
			to PageContactShowBob
		then:
			waitFor { groupList.find('li span').size() == 2 }
	}

	def 'clicking save actually adds contact to newly selected groups'() {
		when:
			to PageContactShowBob
			$('#contact-details select', name:'group-dropdown').value('Others')
			$("#contact-details .save").click()
		then:
			at PageContactShow
			Contact.findByName('Bob') in Group.findByName('Test').members
	}
	
	def 'clicking save actually adds multiple contacts to newly selected groups'() {
		when:
			to PageContactShow
			contactSelect[1].click()
		then:
			waitFor { $('input#name').value() == 'Bob' }
		when:
			contactSelect[0].click()
		then:
			waitFor { multiGroupSelect.find('option').size() > 1 }
		when:
			def otherGroupId = Group.findByName('Others').id
			multiGroupSelect.jquery.val(otherGroupId)
			multiGroupSelect.jquery.trigger('change')
			updateAll.click()	
		then:
			waitFor {Group.findByName('Others').members*.name.containsAll(['Bob', 'Alice'])}
	}
	
	def 'clicking save removes multiple contacts from selected groups'() {
		given:
			def bob = Contact.findByName("Bob")
			def alice = Contact.findByName('Alice')
			def otherGroup = Group.findByName('Others')
			bob.addToGroups(otherGroup,true)
			alice.addToGroups(otherGroup,true)
			assert bob.isMemberOf(otherGroup)
			assert alice.isMemberOf(otherGroup)
		when:
			to PageContactShow
			contactSelect[1].click()
		then:
			waitFor { $('input#name').value() == 'Bob' }
		when:
			contactSelect[0].click()
		then:
			waitFor { $("#multi-group-list #remove-group-${otherGroup.id}").displayed }
		when:
			$("#multi-group-list #remove-group-${otherGroup.id}").click()
			updateAll.click()
		then:
			waitFor { flashMessage.displayed }
		when:
			otherGroup.refresh()
		then:
			otherGroup.members == []
	}
	
	def 'clicking save removes contact from newly removed groups'() {
		when:
			def otherGroup = Group.findByName('Others')
			to PageContactShowBob
			def btnRemoveFromGroup = $("#remove-group-${otherGroup.id}")
			btnRemoveFromGroup.click()
			def btnUpdate = $("#single-contact #update-single")
			btnUpdate.click()
		then:
			at PageContactShow
			otherGroup.refresh()
			GroupMembership.countMembers(otherGroup) == 0
	}
	
	def "should enable save and cancel buttons when new group is added"() {
		when:
			to PageContactShowBob
		then:
			btnSave.disabled
		when:
			$("#group-dropdown").value("Others").click()
		then:
			waitFor { !btnSave.disabled }
			!btnCancel.disabled
			
	}
}

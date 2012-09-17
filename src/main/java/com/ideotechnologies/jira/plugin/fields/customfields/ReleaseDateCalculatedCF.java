package com.ideotechnologies.jira.plugin.fields.customfields;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.DateField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.util.concurrent.LazyReference;
import com.ideotechnologies.jira.plugin.fields.fieldconfig.VersionDateCFConfig;
import com.ideotechnologies.jira.plugin.service.JiraBusinessService;
import com.ideotechnologies.jira.plugin.service.dao.GenericValueDAO;
import com.ideotechnologies.jira.plugin.utils.helpers.UtilsHelper;

public class ReleaseDateCalculatedCF extends AbstractGenericCalculatedCF
		implements DateField {
private JiraBusinessService jiraBusinessService ;

	public ReleaseDateCalculatedCF() {
	super();
	this.jiraBusinessService = new JiraBusinessService();
}

	@Override
	public Object getValueFromIssue(CustomField field, Issue issue) {
		Issue issueSelected = null;
		Object customFieldValue = null ;

		
		if (issue == null) {//For reindexing
			return "";
		}
	
			/*//TODO log pertinent avant l'envoi de la valeur null
			//log :Fix version non renseignee
			if(getFirstFixVersionByIssue(issue)== null){return null;}*/
			
			//TODO log pertinent avant l'envoi de la valeur null
			//log : issue version associe a l'issue introuvable
			issueSelected = jiraBusinessService.getIssueSubtaskByFirstFixVersion(issue, field);
			if(issueSelected == null){
				return null;
				}
			/*//TODO log pertinent avant l'envoi de la valeur null
			//log : L'issue n'est pas une subtask
			//Partie traitement business a mettre dans #getIssueSubtaskByFirstFixVersion
			if(!issueSelected.isSubTask())return null;*/
			
			//TODO Partie business move to #getFeldValueFromIssue ans #getParentIssue
			Issue parentIssue = issueSelected.getParentObject();
			//FieldConfig fieldConfig = field.getRelevantConfig(issue);
			//jiraBusinessService.getFieldValueFromIssue(parentIssue, fieldConfig)	;
			FieldConfig fieldConfig = field.getRelevantConfig(issue);
			
			//TODO log pertinent avant l'envoi de la valeur null
			//log warn: fieldconfig is null
			if(fieldConfig == null)return null; //Resolve anomalies about issue link
			
			CustomField cf = ComponentManager.getInstance().getCustomFieldManager().getCustomFieldObject(
					GenericValueDAO.getFieldToDisplayId(fieldConfig.getId()));
			
			//TODO log pertinent avant l'envoi de la valeur null
			//log : Le champ customfoeld_xxx n'est pas renseigne
			if(!cf.hasValue(parentIssue)){
				return null;
			}
			else{customFieldValue = parentIssue.getCustomFieldValue(cf);}
		
			return customFieldValue;
	}

	@Override
	public Map<String, Object> getVelocityParameters(Issue issue,
			CustomField field, FieldLayoutItem fieldLayoutItem) {
		Map<String, Object> velocityParameters = super.getVelocityParameters(issue, field, fieldLayoutItem);
		DateFieldFormat dateFieldFormat = ComponentAccessor.getComponentOfType(DateFieldFormat.class);
		 velocityParameters.put("dateFieldFormat", dateFieldFormat);
		return velocityParameters;
	}

	@Override
	public List getConfigurationItemTypes() {
		final List<FieldConfigItemType> configItemType   = super.getConfigurationItemTypes();
		configItemType.add(new VersionDateCFConfig());
		return configItemType;
	}
	
}

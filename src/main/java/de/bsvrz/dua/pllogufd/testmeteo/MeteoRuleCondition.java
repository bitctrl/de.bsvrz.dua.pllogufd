package de.bsvrz.dua.pllogufd.testmeteo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MeteoRuleCondition {
	
	private final String templateStr;
	private final List<Object> arguments = new ArrayList<>();
	
	public MeteoRuleCondition(String templateStr, Object[] arguments) {
		this.templateStr = templateStr;
		for( Object argument : arguments) {
			this.arguments.add(argument);
		}
	}

	public String getTemplateStr() {
		return templateStr;
	}

	public List<Object> getArguments() {
		return Collections.unmodifiableList(arguments);
	}
}

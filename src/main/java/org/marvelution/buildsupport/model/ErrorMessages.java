package org.marvelution.buildsupport.model;

import java.util.*;

/**
 * @author Mark Rekveld
 */
public class ErrorMessages
{

	private List<String> errorMessages;
	private Map<String, String> errors = new HashMap<>();

	public List<String> getErrorMessages()
	{
		return errorMessages;
	}

	public void setErrorMessages(List<String> errorMessages)
	{
		this.errorMessages = errorMessages;
	}

	public Map<String, String> getErrors()
	{
		return errors;
	}

	public void setErrors(Map<String, String> errors)
	{
		this.errors = errors;
	}

	@Override
	public String toString()
	{
		return new StringJoiner(", ").add("messages: " + errorMessages).add("errors: " + errors).toString();
	}
}

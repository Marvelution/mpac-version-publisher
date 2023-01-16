/*
 * Copyright (c) 2023-present Marvelution Holding B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.marvelution.buildsupport.configuration;

/**
 * Variables used by the Pipe.
 *
 * @author Mark Rekveld
 */
public class Variables
{

	public static final String DEBUG = "DEBUG";
	public static final String DRY_RUN = "DRY_RUN";
	public static final String MARKETPLACE = "MARKETPLACE";
	public static final String VERSION_ARTIFACT = "VERSION_ARTIFACT";
	public static final String VERSION_STATUS = "VERSION_STATUS";
	public static final String VERSION_PAYMENT_MODEL = "VERSION_PAYMENT_MODEL";
	public static final String JIRA_API = "JIRA_API";
	public static final String JIRA_PROJECT_KEY = "JIRA_PROJECT_KEY";
	public static final String JIRA_VERSION_FORMAT = "JIRA_VERSION_FORMAT";
	public static final String ADDITIONAL_JQL = "ADDITIONAL_JQL";
	public static final String ISSUE_SECURITY_LEVEL_FILTER = "ISSUE_SECURITY_LEVEL_FILTER";
	private static final String BASE_URL = "_BASE_URL";
	public static final String MARKETPLACE_BASE_URL = MARKETPLACE + BASE_URL;
	public static final String JIRA_BASE_URL = "JIRA" + BASE_URL;
	private static final String USER = "_USER";
	public static final String MARKETPLACE_USER = MARKETPLACE + USER;
	public static final String JIRA_API_USER = JIRA_API + USER;
	private static final String TOKEN = "_TOKEN";
	public static final String MARKETPLACE_TOKEN = MARKETPLACE + TOKEN;
	public static final String JIRA_API_TOKEN = JIRA_API + TOKEN;
}

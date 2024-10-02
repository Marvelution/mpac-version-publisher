# MPAC Version Publisher

A CLI tool to publish new app versions to the Atlassian Marketplace.
Simply run the tool: `java -jar mpac-version-publisher-1-SNAPSHOT.jar`.
If additional arguments are provided [CommandLinePublisherConfiguration](#CommandLinePublisherConfiguration) is used.
Otherwise, [EnvironmentPublisherConfiguration](#EnvironmentPublisherConfiguration) is used.

## CommandLinePublisherConfiguration

Configuring the tool using the `org.marvelution.buildsupport.configuration.CommandLinePublisherConfiguration` takes the following
arguments:

```bash
$ java -jar mpac-version-publisher-1-SNAPSHOT.jar -h

Missing required options: mu, mt, vap
usage: jar [-D] [-dr] [-h] [-isf] [-j <arg>] [-jpk <arg>] [-jql <arg>]
[-jt <arg>] [-ju <arg>] [-jvf <arg>] [-m <arg>] -mt <arg> -mu <arg>
[-pm <arg>] [-rnp <arg>] -vap <arg> [-vs <arg>] [-w <arg>]
-D,--debug                           Enable debug logging
-dr,--dry-run                        Dry run the publishing
-h,--help                            Print this help message
-isf                                 Use Issue Security Filter
-j,--jira-base-url <arg>             Jira base url
-jpk,--jira-project-key <arg>        Jira project key
-jql <arg>                           Additional JQL
-jt,--jira-token <arg>               Jira token
-ju,--jira-user <arg>                Jira username
-jvf,--jira-version-format <arg>     Jira version format
-m,--marketplace-base-url <arg>      Atlassian Marketplace base url
-mt,--marketplace-token <arg>        Atlassian Marketplace token
-mu,--marketplace-user <arg>         Atlassian Marketplace username
-pm,--payment-model <arg>            Payment Model
-rnp,--release-notes-path <arg>      Release notes path
-vap,--version-artifact-path <arg>   Version artifact path
-vs,--version-status <arg>           Version Status
-w,--work-dir <arg>                  Work directory
```

## EnvironmentPublisherConfiguration

Configuring the tool via the `org.marvelution.buildsupport.configuration.EnvironmentPublisherConfiguration` takes the following
environment variables:

| Variable                    | Usage                                                                                                                                                                           |
|-----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| MARKETPLACE_BASE_URL        | URL of the Marketplace instance, `https://marketplace.atlassian.com` by default.                                                                                                |
| MARKETPLACE_USER (*)        | Username of the Atlassian account for Authentication. Example: `human` or `human@example.com`                                                                                   |
| MARKETPLACE_TOKEN (*)       | Password of the Atlassian account for Authentication.                                                                                                                           |
| VERSION_ARTIFACT (*)        | The new version file to upload to the Marketplace.                                                                                                                              |
| VERSION_STATUS              | The version status, `public` or `private`, to publish the version as, `public` by default.                                                                                      |
| VERSION_PAYMENT_MODEL       | The version payment model, either `free`, `vendor` or `atlassian`. `free` by default.                                                                                           |
| JIRA_BASE_URL               | URL of Jira instance. Example: `https://<yourdomain>.atlassian.net` or `https://issues.<yourdomain>.com`                                                                        |
| JIRA_API_USER               | Username (Jira Server/Data Center) or Email address (Jira Cloud). Example: `human` or `human@example.com`                                                                       |
| JIRA_API_TOKEN              | **Password**/**Access Token** for Authorization. Example: `HXe8DGg1iJd2AopzyxkFB7F2` ([Jira Cloud How To](https://confluence.atlassian.com/cloud/api-tokens-938839638.html))    |
| JIRA_PROJECT_KEY            | Key of the Jira project that contains the version that should be released.                                                                                                      |
| JIRA_VERSION_FORMAT         | Format used to get the Jira version name from the artifact version, `%s` by default. Example: `server-%s`.                                                                      |
| ADDITIONAL_JQL              | Additional JQL to collect the Jira issues associated with the add-on version for release notes. Fields `projectKey`, `fixVersion` and `statusCategory` are added automatically. |
| ISSUE_SECURITY_LEVEL_FILTER | Turn on to hide issues from the release notes that have a security level set, equivalent to JQL `level is EMPTY`. `false` by default.                                           |
| DEBUG                       | Turn on extra debug information. `false` by default.                                                                                                                            |

_(*) = required variable._


## Development

### Toolchain

You will need to have a toolchain for the jdk11

`vim  ~/.m2/toolchains.xml`

Be sure to adjust path to you jvm11 installation

```xml
<toolchains>
  <!-- JDK toolchains -->
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>11</version>
      <vendor>librca</vendor>
    </provides>
    <configuration>
      <jdkHome>/your/path/to/your/jvm11</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
```

### Build

```bash
make build-all
# or
./mvnw clean package
```

### Test

```bash
make test
# or
./mvnw clean test
```

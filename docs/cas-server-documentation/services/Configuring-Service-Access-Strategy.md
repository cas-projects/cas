---
layout: default
title: CAS - Configuring Service Access Strategy
category: Services
---

{% include variables.html %}

# Configure Service Access Strategy

The access strategy of a registered service provides fine-grained control over the service authorization rules.
It describes whether the service is allowed to use the CAS server, allowed to participate in
single sign-on authentication, etc. Additionally, it may be configured to require a certain set of principal
attributes that must exist before access can be granted to the service. This behavior allows one to configure
various attributes in terms of access roles for the application and define rules that would be enacted and
validated when an authentication request from the application arrives.

## Default Strategy

The default strategy allows one to configure a service with the following properties:

| Field                             | Description
|-----------------------------------|---------------------------------------------------------------------------------
| `enabled`                         | Flag to toggle whether the entry is active; a disabled entry produces behavior equivalent to a non-existent entry.
| `ssoEnabled`                      | Set to `false` to force users to authenticate to the service regardless of protocol flags (e.g. `renew=true`).
| `requiredAttributes`              | A `Map` of required principal attribute names along with the set of values for each attribute. These attributes **MUST** be available to the authenticated Principal and resolved before CAS can proceed, providing an option for role-based access control from the CAS perspective. If no required attributes are presented, the check will be entirely ignored.
| `requireAllAttributes`            | Flag to toggle to control the behavior of required attributes. Default is `true`, which means all required attribute names must be present. Otherwise, at least one matching attribute name may suffice. Note that this flag only controls which and how many of the attribute **names** must be present. If attribute names satisfy the CAS configuration, at the next step at least one matching attribute value is required for the access strategy to proceed successfully.
| `unauthorizedRedirectUrl`         | Optional url to redirect the flow in case service access is not allowed.
| `caseInsensitive`                 | Indicates whether matching on required attribute values should be done in a case-insensitive manner. Default is `false`
| `rejectedAttributes`              | A `Map` of rejected principal attribute names along with the set of values for each attribute. These attributes **MUST NOT** be available to the authenticated Principal so that access may be granted. If none is defined, the check is entirely ignored.

<div class="alert alert-info"><strong>Are we sensitive to case?</strong><p>Note that comparison of principal/required attribute <strong>names</strong> is
case-sensitive. Exact matches are required for any individual attribute name.</p></div>

<div class="alert alert-info"><strong>Released Attributes</strong><p>Note that if the CAS server is configured to cache attributes upon release, all required attributes must also be released to the relying party. <a href="../integration/Attribute-Release.html">See this guide</a> for more info on attribute release and filters.</p></div>

### Examples

The following examples demonstrate access policy enforcement features of CAS.

#### Disable Service Access

Service is not allowed to use CAS:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "enabled" : false,
    "ssoEnabled" : true
  }
}
```

#### Enforce Attributes

To access the service, the principal must have a `cn` attribute with the value of `admin` **AND** a
`givenName` attribute with the value of `Administrator`:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "enabled" : true,
    "ssoEnabled" : true,
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ "admin" ] ],
      "givenName" : [ "java.util.HashSet", [ "Administrator" ] ]
    }
  }
}
```

To access the service, the principal must have a `cn` attribute with the value of `admin` **OR** a
`givenName` attribute with the value of `Administrator`:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "enabled" : true,
    "ssoEnabled" : true,
    "requireAllAttributes": false,
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ "admin" ] ],
      "givenName" : [ "java.util.HashSet", [ "Administrator" ] ]
    }
  }
}
```

To access the service, the principal must have a `cn` attribute whose value is either of `admin`, `Admin` or `TheAdmin`.

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "enabled" : true,
    "ssoEnabled" : true,
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ "admin", "Admin", "TheAdmin" ] ]
    }
  }
}
```

<div class="alert alert-info"><strong>Supported Syntax</strong><p>Required values for 
a given attribute support regular expression patterns. For example, a <code>phone</code> attribute could
require a value pattern of <code>\d\d\d-\d\d\d-\d\d\d\d</code>.</p></div>

#### Static Unauthorized Redirect URL

Service access is denied if the principal does *not* have a `cn` attribute containing the value `super-user`. If so,
the user will be redirected to `https://www.github.com` instead.

```json
{
  "@class": "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id": 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "unauthorizedRedirectUrl" : "https://www.github.com",
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ "super-user" ] ]
    }
  }
}
```

#### Dynamic Unauthorized Redirect URL

Service access is denied if the principal does *not* have a `cn` attribute containing the value `super-user`. If so, the redirect URL
will be dynamically determined based on outcome of the specified Groovy script.

```json
{
  "@class": "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id": 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "unauthorizedRedirectUrl" : "file:/etc/cas/config/unauthz-redirect-url.groovy",
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ "super-user" ] ]
    }
  }
}
```

The script itself will take the following form:

```groovy
import org.apereo.cas.*
import org.apereo.cas.web.support.*
import java.util.*
import java.net.*
import org.apereo.cas.authentication.*

URI run(final Object... args) {
    def registeredService = args[0]
    def requestContext = args[1]
    def applicationContext = args[2]
    def logger = args[3]
    
    logger.info("Redirecting to somewhere, processing [{}]", registeredService.name)
    /**
     * Stuff Happens...
     */
    return new URI("https://www.github.com");
}
```

The following parameters are provided to the script:

| Field                             | Description
|-----------------------------------|---------------------------------------------------------------------------------
| `registeredService`   | The object representing the matching registered service in the registry.
| `requestContext`      | The object representing the Spring Webflow `RequestContext`.
| `applicationContext`  | The object representing the Spring `ApplicationContext`.
| `logger`              | The object responsible for issuing log messages such as `logger.info(...)`.

#### Enforce Combined Attribute Conditions

To access the service, the principal must have a `cn` attribute whose value is either of `admin`, `Admin` or `TheAdmin`,
**OR** the principal must have a `member` attribute whose value is either of `admins`, `adminGroup` or `staff`.


```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "enabled" : true,
    "requireAllAttributes" : false,
    "ssoEnabled" : true,
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ "admin", "Admin", "TheAdmin" ] ],
      "member" : [ "java.util.HashSet", [ "admins", "adminGroup", "staff" ] ]
    }
  }
}
```

#### Enforce Must-Not-Have Attributes

To access the service, the principal must have a `cn` attribute whose value is either of `admin`, `Admin` or `TheAdmin`,
OR the principal must have a `member` attribute whose value is either of `admins`, `adminGroup` or `staff`. The principal
also must not have an attribute "role" whose value matches the pattern `deny.+`.


```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "enabled" : true,
    "requireAllAttributes" : false,
    "ssoEnabled" : true,
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ "admin", "Admin", "TheAdmin" ] ],
      "member" : [ "java.util.HashSet", [ "admins", "adminGroup", "staff" ] ]
    },
    "rejectedAttributes" : {
      "@class" : "java.util.HashMap",
      "role" : [ "java.util.HashSet", [ "deny.+" ] ]
    }
  }
}
```

<div class="alert alert-info"><strong>Supported Syntax</strong><p>Rejected values for 
a given attribute support regular expression patterns. For example, a <code>role</code> attribute could
be designed with a value value pattern of <code>admin-.*</code>.</p></div>

## Groovy Script

Access strategy and authorization decision can be carried using a Groovy script for all services and applications. This policy
is not tied to a specific application and is invoked for all services and integrations. 

{% include_cached casproperties.html properties="cas.access-strategy.groovy" %}

The outline of the script is as follows:

```groovy
import org.apereo.cas.audit.*
import org.apereo.cas.services.*

def run(Object[] args) {
    def context = args[0] as AuditableContext
    def logger = args[1]
    logger.debug("Checking access for ${context.registeredService}")
    def result = AuditableExecutionResult.builder().build()
    result.setException(new UnauthorizedServiceException("Service unauthorized"))
    return result
}
```
      
The following parameters are passed to the script:

| Parameter  | Description
|-------------|---------------------------------------------------------------------------------
| `context`   | An `AuditableContext` object that carries auditable data such as registered services, authentication, etc. 
| `logger`    | The object responsible for issuing log messages such as `logger.info(...)`.
  

## Time-Based

The time-based access strategy is an extension of the default which additionally,
allows one to configure a service with the following properties:

| Field                             | Description
|-----------------------------------|---------------------------------------------------------------------------------
| `startingDateTime`                | Indicates the starting date/time whence service access may be granted.  (i.e. `2015-10-11T09:55:16.552-07:00`)
| `endingDateTime`                  | Indicates the ending date/time whence service access may be granted.  (i.e. `2015-10-20T09:55:16.552-07:00`)

Service access is only allowed within `startingDateTime` and `endingDateTime`:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "test",
  "id" : 62,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.TimeBasedRegisteredServiceAccessStrategy",
    "enabled" : true,
    "ssoEnabled" : true,
    "unauthorizedRedirectUrl" : "https://www.github.com",
    "startingDateTime" : "2015-11-01T13:19:54.132-07:00",
    "endingDateTime" : "2015-11-10T13:19:54.248-07:00",
    "zoneId" : "UTC"
  }
}
```

The configuration of the public key component qualifies to use the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.


## Remote Endpoint

This strategy is an extension of the default which additionally,
allows one to configure a service with the following properties:

| Field                             | Description
|-----------------------------------|---------------------------------------------------------------------------------
| `endpointUrl`                | Endpoint that receives the authorization request from CAS for the authenticated principal. 
| `acceptableResponseCodes`    | Comma-separated response codes that are considered accepted for service access.

The objective of this policy is to ensure a remote endpoint can make service access decisions by
receiving the CAS authenticated principal as url parameter of a `GET` request. The response code that
the endpoint returns is then compared against the policy setting and if a match is found, access is granted.

Remote endpoint access strategy authorizing service access based on response code:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "^https://.+",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.RemoteEndpointServiceAccessStrategy",
    "endpointUrl" : "https://somewhere.example.org",
    "acceptableResponseCodes" : "200,202"
  }
}
```

## Groovy

This strategy delegates to a Groovy script to dynamically decide the access rules requested by CAS at runtime:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "^https://.+",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.GroovyRegisteredServiceAccessStrategy",
    "groovyScript" : "file:///etc/cas/config/access-strategy.groovy"
  }
}
```

The script itself may be designed as such by overriding the needed operations where necessary:

```groovy
import org.apereo.cas.services.*
import java.util.*

class GroovyRegisteredAccessStrategy extends DefaultRegisteredServiceAccessStrategy {
    @Override
    boolean isServiceAccessAllowed() {
        ...
    }

    @Override
    boolean isServiceAccessAllowedForSso() {
        ...
    }

    @Override
    boolean doPrincipalAttributesAllowServiceAccess(String principal, Map<String, Object> attributes) {
        ...
    }
}
```

The configuration of this component qualifies to use the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax. Refer to the CAS API documentation to learn more about operations and expected behaviors.

## Grouper

The grouper access strategy is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-grouper-core" %}

This access strategy attempts to locate [Grouper](https://incommon.org/software/grouper/) 
groups for the CAS principal. The groups returned by Grouper
are collected as CAS attributes and examined against the list of required attributes for service access.

The following properties are available:

| Field        | Description                                                                       | Values
|--------------|-----------------------------------------------------------------------------------|-----------------------------------------------------------
| `groupField` | Attribute of the Grouper group used when converting the group to a CAS attribute. | `NAME`, `EXTENSION`, `DISPLAY_NAME` or `DISPLAY_EXTENSION`.

You will also need to ensure `grouper.client.properties` is available on the classpath (i.e. `src/main/resources`)
with the following configured properties:

```properties
grouperClient.webService.url = http://grouper.example.com/grouper-ws/servicesRest
grouperClient.webService.login = banderson
grouperClient.webService.password = password
```

Grouper access strategy based on group's display extension:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "test",
  "id" : 62,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.grouper.services.GrouperRegisteredServiceAccessStrategy",
    "enabled" : true,
    "ssoEnabled" : true,
    "requireAllAttributes" : true,
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "grouperAttributes" : [ "java.util.HashSet", [ "faculty" ] ]
    },
    "groupField" : "DISPLAY_EXTENSION"
  }
}
```
      
While the `grouper.client.properties` is a hard requirement and must be presented, configuration properties can always be assigned to the strategy
to override the defaults: 

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "test",
  "id" : 62,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.grouper.services.GrouperRegisteredServiceAccessStrategy",
    "configProperties" : {
      "@class" : "java.util.HashMap",
      "grouperClient.webService.url" : "http://grouper.example.com/grouper-ws/servicesRest"
    },
    "groupField" : "DISPLAY_EXTENSION"
  }
}
```

---
layout: default
title: CAS - Eureka Service Discovery
category: High Availability
---
{% include variables.html %}


# Eureka Server Discovery Service

[Eureka](https://github.com/Netflix/eureka) is a REST-based service that is primarily 
used for locating services for the purpose of load balancing and failover of middle-tier servers. Eureka provides 
both a discovery server and also support for clients which would be the individual CAS servers themselves in the pool. 
The server can be configured and deployed to be highly available, with each server replicating state about the registered services to the others.

CAS provides a Eureka-enabled service discovery server that is based on [Spring Cloud Netflix](http://cloud.spring.io/spring-cloud-netflix) 
and bootstrapped via [Spring Cloud](http://cloud.spring.io/spring-cloud-static/spring-cloud.html).

### Installation

The Eureka discovery server is not part of the CAS server and
is a standalone web application which can be deployed using the [CAS Initializr](../installation/WAR-Overlay-Initializr.html).

You may also ook for a suitable and relevant ready-made Docker image via `docker search eureka`.

When deployed the following URLs become available:

| URL                | Description
|--------------------|-----------------------------------------------
| `/`                | Home page listing service registrations.
| `/eureka/apps`     | Raw registration metadata.

### High Availability Mode

You always want to make sure the discovery server is run in high-availability mode. One option is to 
ensure each individual Eureka server is peer aware. See [this guide](http://cloud.spring.io/spring-cloud-static/spring-cloud.html#_peer_awareness) to learn how to manage that.

## CAS Discovery Service Clients

Each individual CAS server is given the ability to auto-register itself 
with the discovery server, provided configuration is made available to 
instruct the CAS server how to locate and connect to the discover server service.

Support is added by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-eureka-client" %}

{% include_cached casproperties.html thirdParty="eureka.client,spring.cloud.config.discovery" %}

### Authentication

Support for HTTP basic authentication will be automatically added if one of Eureka server URLs 
in the configuration has credentials embedded in it (curl style, like `http://user:password@localhost:8761/eureka`). 

### Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
<Logger name="org.springframework.cloud" level="debug" additivity="false">
  <AppenderRef ref="casConsole"/>
  <AppenderRef ref="casFile"/>
</Logger>
<Logger name="com.netflix" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
```

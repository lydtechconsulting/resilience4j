# Circuit Breaker & Failover Example Using Resilience4j

# Overview

Resilience4j is an open source library offering many features for managing fault tolerance in an application. It is viewed as the recommended choice and natural successor to the now end-of-life Spring Cloud Hystrix libraries. It supports a number of resilience patterns including failover, circuit breaker, retry, bulkhead, cache, and timelimiter. It is straightforward to integrate with Spring Boot, with a Resillience4j Spring Boot library being available.

This repo accompanies the following article:

- [Failover and Circuit Breaker with Resilience4j](https://www.lydtechconsulting.com/blog-resilience4j.html):  Using the Resilience4j library for failover and circuit breaker.

# Demo Application

For this demo a Spring Boot application has been developed which illustrates these patterns. A REST API provides an endpoint which when called attempts to lookup the account details for the provided request parameters, iban, country and currency. The application can be viewed in full in github, and is referenced throughout this blog.

The application is a Spring Boot application. Spring Boot has first class support for Resilience4j, with provision of a resilience4j-spring-boot2 library.

Included in the project are integration tests that are used to demonstrate the functionality. Wiremock is used to mock the third party provided (3PP) services allowing simulation of the required behaviour that results in failover and the circuit breaker state changes being triggered.

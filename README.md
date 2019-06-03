# Customs API Common

[ ![Download](https://api.bintray.com/packages/hmrc/releases/customs-api-common/images/download.svg) ](https://bintray.com/hmrc/releases/customs-api-common/_latestVersion)


A play module library that contains common infrastructure code for Customs micro services.

It also has a DynamicStubbingService and routes to enable the switching of connector URLs from real to stubbed services.

## Migration of existing micro services to use this library

### SBT dependencies
Add a dependency to this library in `AppDependencies.scala`

    val customsApiCommon = "uk.gov.hmrc" %% "customs-api-common" % customsApiCommonVersion

Add this in `build.sbt`

    val compileDependencies = Seq(..., customsApiCommon)

### SBT Test Dependencies

When adding test dependencies add the following to `AppDependencies.scala` 
    
    val customsApiCommonTests = "uk.gov.hmrc" %% "customs-api-common" % customsApiCommonVersion % testScope classifier "tests"
    
And add this to `build.sbt`
    
    val testDependencies = Seq(..., customsApiCommonTests)

Ensure that sbt version is at least 0.13.12 (due to this [bug](https://github.com/sbt/sbt/issues/2002))

### application.conf
Make sure you have the following entries

    application.logger.name="YOUR_APP_LOGGER_NAME"

    microservice {
      ...      
      services {
        ...
      }
    }
      
    # Play Modules
    # ~~~~
    # Additional play modules can be added here
    play.modules.enabled += "uk.gov.hmrc.customs.api.common.CustomsApiCommonModule"

    play.http.errorHandler = "uk.gov.hmrc.customs.api.common.config.CustomsErrorHandler"
    
For more details on configuration see [here](https://github.com/hmrc/bootstrap-play-25/). 

##To add Dynamic Service Configuration
Add configuration in your 'application.conf', additional environments are added to the service.
Dynamic service configuration may be used for stubs as well as other environments
    
    services {
      your-service {
        host = some.host
        port = 80
        bearer-token = ...
        context = /osb/submitdeclaration/1.0.0
        
        environment {
            host = other.host
            port = 80
            bearer-token = ...
            context = /osb/submitdeclaration/1.0.0
        }
        
        stub {
            host = localhost
            port = 9477
            bearer-token = ...
            context = /submitdeclaration
        }
      }
     }

Create test only routes file `testOnlyDoNotUseInAppConf.routes` with contents

    ->        /test-only/service               dynamicservice.Routes

    ->        /                                prod.Routes

In your connector inject the ServiceConfigProvider

    @Singleton
    class MyConnector @Inject()(.... // other dependencies
                                configProvider: ServiceConfigProvider)
                                
Call the service to get the dynamically switched service configuration and use it in the HTTP method

    ...
    val serviceConfig = configProvider.getConfg("your-service")
    ws.url(serviceConfig.url).withHeaders(headers: _*)
    ...

You will need to run the microservice with the `-Dapplication.router=testOnlyDoNotUseInAppConf.Routes` JVM parameter set.

To switch service configuration (replace 'environment' property in the body with the name of the configured environment
you would like to point the service to):

    POST /test-only/service/:service-name/configuration
    
    Headers:
        Content-Type: application/json
        
    Body: { "environment": "stub" }
    
    Use the environment name 'default' to switch back to the root configuration

To view the current configuration for a service:
    
    GET /test-only/service/:service-name
    
Example response:

    {
      "service": "your-service",
      "environment": "default",
      "url": "http://some.host:80/osb/submitdeclaration/1.0.0",
      "bearerToken": "..."
    }

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

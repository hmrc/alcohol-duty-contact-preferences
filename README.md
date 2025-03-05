# alcohol-duty-contact-preferences

This is the backend service to capture an alcohol producers communication preference

## Test only endpoints

### Event hub bounced email endpoint

This is a test only endpoint used for checking that we can successfully integrate with Event Hub in the local environment.
In future, this endpoint will be used by Event Hub in the instance that an email sent out has bounced. When this happens
we will tell ETMP that this has happened. At the moment this functionality has not been implemented, but will be during
the development of the ECP microservice.

The endpoint route is:
> `POST /event-hub/bounce`

For it to work you need this service running with test only routes enabled:
> `sbt "run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes"`

and all the event hub running service manager with the correct config. This can be added alongside all ADR microservices
via:
> `sm2 --start ALCOHOL_DUTY_ALL`

or independently with:
> `sm2 --start EVENT_HUB_FOR_ADR`

An example request, via an API client like Bruno, to Event Hub, to hit this endpoint is:

```
curl -v -X POST -H "Content-Type: application/json" http://localhost:9050/event-hub/publish/email -d '
{
    "eventId": "623b6f96-d36f-4014-8874-7f3f8287f9e6", 
    "subject": "calling", 
    "groupId": "su users",
    "timestamp": "2021-07-01T13:09:29Z",
    "event" : {
        "event": "failed",
        "emailAddress": "hmrc-customer@some-domain.org",
        "detected": "2021-04-07T09:46:29+00:00",
        "code": 605,
        "reason": "Not delivering to previously bounced address",
        "enrolment": "HMRC-CUS-ORG~EORINumber~GB123456789"
    }
}'
```

to hit this endpoint directly, without going through event hub you can use this request:

```
curl -v -X POST -H "Content-Type: application/json" http://localhost:16006/alcohol-duty-contact-preferences/test-only/event-hub/bounce -d '
{
    "eventId": "550e8400-e29b-41d4-a716-446655440000",
    "subject": "testSubject",
    "groupId": "testGroupId",
    "timestamp": "2021-07-01T13:09:29",
    "event": {
        "event":"failed",
        "emailAddress":"hmrc-customer@some-domain.org",
        "detected":"2021-04-07T09:46:29+00:00",
        "code":605,
        "reason":"Not delivering to previously bounced address",
        "enrolment":"HMRC-CUS-ORG~EORINumber~GB123456789"
    }
}'
```

### License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
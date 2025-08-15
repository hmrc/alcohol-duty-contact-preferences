# alcohol-duty-contact-preferences

This is the backend microservice to capture an alcohol producer's communication preference.

## API Endpoints

- [Create User Answers](api-docs/createUserAnswers.md): `POST /alcohol-duty-contact-preferences/user-answers`
- [Get User Answers](api-docs/getUserAnswers): `GET /alcohol-duty-contact-preferences/user-answers/:appaId`
- [Set User Answers](api-docs/setUserAnswers): `PUT /alcohol-duty-contact-preferences/user-answers`
- [Get Email Verification Status](https://github.com/hmrc/email-verification?tab=readme-ov-file#get-verification-status):
  `GET /alcohol-duty-contact-preferences/get-email-verification/:credId` (link to email-verification README)
- [Submit Contact Preferences](api-docs/submitContactPreferences.md):
  `PUT /alcohol-duty-contact-preferences/submit-preferences/:appaId`
- [Handle Bounced Email](api-docs/handleBouncedEmail.md):
  `POST /alcohol-duty-contact-preferences/event-hub/bounce`

## Running the service

> `sbt run`

The service runs on port `16006` by default.

## Test only endpoints

To run the service with test only routes enabled:
> `sbt "run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes"`

### Clear all data

This endpoint clears all user answers data in the repository.
> `DELETE /alcohol-duty-contact-preferences/test-only/user-answers/clear-all`

## Testing the event hub bounced email endpoint

To test this endpoint, you need this service running locally and the event hub running in service manager with the
correct config. You can start all ADR microservices, then stop this microservice as follows:
> `sm2 --start ALCOHOL_DUTY_CONTACT_PREFERENCES_ALL`
> `sm2 --stop ALCOHOL_DUTY_CONTACT_PREFERENCES`

Event hub can be started independently with:
> `sm2 --start EVENT_HUB_FOR_ADR`

For an example request, via an API client like Bruno, to Event Hub, see
the [readme for this endpoint](api-docs/handleBouncedEmail.md).

## Running tests

### Unit tests

> `sbt test`

### Integration tests

> `sbt it/test`

## Scalafmt and Scalastyle

To check if all the scala files in the project are formatted correctly:
> `sbt scalafmtCheckAll`

To format all the scala files in the project correctly:
> `sbt scalafmtAll`

To check if there are any scalastyle errors, warnings or infos:
> `sbt scalastyle`

### All tests and checks

This is an sbt command alias specific to this project. It will run a scala format
check, run a scala style check, run unit tests, run integration tests and produce a coverage report:
> `sbt runAllChecks`

### License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
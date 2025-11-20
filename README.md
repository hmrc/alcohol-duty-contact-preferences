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

## Running tests

### Unit tests

> `sbt test`

### Integration tests

> `sbt it/test`

## Scalafmt

To check if all the scala files in the project are formatted correctly:
> `sbt scalafmtCheckAll`

To format all the scala files in the project correctly:
> `sbt scalafmtAll`

### All tests and checks

This is an sbt command alias specific to this project. It will run a scala format
check, run a scala style check, run unit tests, run integration tests and produce a coverage report:
> `sbt runAllChecks`

### License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
# Handle Bounced Email

This endpoint is used by Event Hub in the instance that an email to an alcohol producer has bounced. When this happens,
we update the user's contact preference in ETMP to post, with the bounced email indicator set to true.

**URL**: `/alcohol-duty-contact-preferences/event-hub/bounce`

**Method**: `POST`

**Request Body**:

| Field Name           | Description                                         | Data Type    | Mandatory/Optional | Notes                                          |
|----------------------|-----------------------------------------------------|--------------|--------------------|------------------------------------------------|
| eventId              | The event id                                        | UUID         | Mandatory          |                                                |
| subject              | The event subject                                   | String       | Mandatory          | Non-empty string                               |
| groupId              | The group id                                        | String       | Mandatory          |                                                |
| timestamp            | The timestamp for this event instance               | Timestamp    | Mandatory          | e.g. 2021-07-01T13:09:29Z                      |
| event                | Object containing event details                     | EventDetails | Mandatory          |                                                |
| event.event          | Brief description of the event                      | String       | Mandatory          | e.g. failed                                    |
| event.emailAddress   | The user's email address                            | String       | Mandatory          |                                                |
| event.detected       | The time the event was detected                     | Timestamp    | Mandatory          | e.g. 2021-04-07T09:46:29+00:00                 |
| event.code           | Status code for the event (as defined by Event Hub) | Numeric      | Mandatory          |                                                |
| event.reason         | Reason for the event being fired                    | String       | Mandatory          | Should be present for ADR bounced email events |
| event.tags           | The optional tags (new)                             | String       | Optional           |                                                |
| event.tags.enrolment | The user's enrolment details                        | String       | Optional           | e.g. HMRC-AD-ORG~APPAID~XMADP0000100208        |

See the [Event Hub README](https://github.com/hmrc/event-hub?tab=readme-ov-file#event-hub) for more information.
See the [Event Hub Confluence](https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?spaceKey=DCT&title=Event+Hub) for more information.

To hit this endpoint directly without going through Event Hub, you can use this request:

```
curl -v -X POST -H "Content-Type: application/json" http://localhost:16006/alcohol-duty-contact-preferences/event-hub/bounce -d '
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
        "tags" : {
			"enrolment":"HMRC-AD-ORG~APPAID~XMADP0000100208"
		}
    }
}'
```

## Responses

### Success response

**Code**: `200 OK`

**Response Body**

The response body is the same as for the submit contact preferences endpoint.

| Field Name       | Description                      | Data Type | Mandatory/Optional | Notes                |
|------------------|----------------------------------|-----------|--------------------|----------------------|
| processingDate   | The date and time of processing  | Timestamp | Mandatory          | YYYY-MM-DDThh:mm:ssZ |
| formBundleNumber | The form bundle number from ETMP | String    | Mandatory          |                      |

**Response Body Examples**

```json
{
  "processingDate": "2025-01-31T09:26:17Z",
  "formBundleNumber": "910000000000"
}
```

### Error responses

**Code**: `400 BAD_REQUEST`
This response can occur if ETMP returns a BAD_REQUEST while performing submission, or the enrolment identifier in the
request body is not in the correct format

**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any consumer without an authorized session that has an ADR enrolment.

**Code**: `404 NOT_FOUND`
This response can occur if ETMP returns a NOT_FOUND error

**Code**: `422 UNPROCESSABLE_ENTITY`
ETMP validation errors: ETMP successfully received the data without any technical issues, but it is unable to process
the data further.

**Code**: `500 INTERNAL_SERVER_ERROR`
This response can occur if ETMP returns another error, or the request body could not be parsed

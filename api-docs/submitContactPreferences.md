# Submit Contact Preferences

Submits contact preferences for a specific appaId.

Calls to this API must be made by an authenticated and authorised user with an ADR enrolment.

**URL**: `/alcohol-duty-contact-preferences/submit-preferences/:appaId`

**Method**: `PUT`

**URL Params**:

| Parameter Name | Type   | Description | Notes |
|----------------|--------|-------------|-------|
| appaId         | String | The appa Id |       |

**Required Request Headers**:

| Header Name   | Header Value   | Description                                |
|---------------|----------------|--------------------------------------------|
| Authorization | Bearer {TOKEN} | A valid bearer token from the auth service |

**Request Body**:

| Parameter Name      | Type    | Description                                             | Mandatory/Optional | Notes                                                                |
|---------------------|---------|---------------------------------------------------------|--------------------|----------------------------------------------------------------------|
| paperlessPreference | Boolean | Whether the user's contact preference is email          | Mandatory          | "1" for true, "0" for false                                          |
| emailAddress        | String  | The user's email address                                | Optional           |                                                                      |
| emailVerification   | Boolean | Whether the user's email address is verified            | Optional           | "1" for true, "0" for false. Required if paperlessPreference is true |
| bouncedEmail        | Boolean | Whether emails to the user's email address have bounced | Optional           | "1" for true, "0" for false                                          |

**Request Body Examples**

***An example contact preference submission request:***

```json
{
  "paperlessPreference": "1",
  "emailAddress": "john.doe@example.com",
  "emailVerification": "1",
  "bouncedEmail": "0"
}
```

## Responses

### Success response

**Code**: `200 OK`

**Response Body**

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
This response can occur if ETMP returns a BAD_REQUEST while performing submission

**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any consumer without an authorized session that has an ADR enrolment.

**Code**: `404 NOT_FOUND`
This response can occur if ETMP returns a NOT_FOUND error

**Code**: `422 UNPROCESSABLE_ENTITY`
ETMP validation errors: ETMP successfully received the data without any technical issues, but it is unable to process
the data further.

**Code**: `500 INTERNAL_SERVER_ERROR`
This response can occur if ETMP returns another error

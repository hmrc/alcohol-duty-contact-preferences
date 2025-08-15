# Set User Answers

Updates the user answers for a specific appaId.

Calls to this API must be made by an authenticated and authorised user with an ADR enrolment in order for the data to be
returned.

**URL**: `/alcohol-duty-contact-preferences/user-answers`

**Method**: `PUT`

**Request Body**

The data section contains the actual user answers. The content will depend on the specific questions answered and will
not be documented in detail here.

| Field Name                                | Description                                                       | Data Type           | Mandatory/Optional | Notes                          |
|-------------------------------------------|-------------------------------------------------------------------|---------------------|--------------------|--------------------------------|
| appaId                                    | The appaId                                                        | String              | Mandatory          |                                |
| userId                                    | The user's id                                                     | String              | Mandatory          |                                |
| subscriptionSummary                       | The user's existing contact preferences from the subscription API | SubscriptionSummary | Mandatory          |                                |
| subscriptionSummary.paperlessReference    | Whether the user is currently on email                            | Boolean             | Mandatory          |                                |
| subscriptionSummary.emailAddress          | The user's existing email address in ETMP                         | String              | Optional           | Encrypted in the backend       |
| subscriptionSummary.emailVerification     | Whether the user's existing email address is verified             | Boolean             | Optional           |                                |
| subscriptionSummary.bouncedEmail          | Whether emails to the user's existing email address have bounced  | Boolean             | Optional           |                                |
| subscriptionSummary.correspondenceAddress | The user's correspondence address (lines 1-4 and postcode)        | String              | Mandatory          |                                |
| subscriptionSummary.countryCode           | The country code of the user's correspondence address             | String              | Optional           |                                |
| emailAddress                              | The email address submitted by the user                           | String              | Optional           | Encrypted in the backend       |
| verifiedEmailAddresses                    | The email addresses that are already verified for the user        | Set(String)         | Mandatory          | Encrypted in the backend       | 
| data                                      | The user answers data                                             | Object              | Mandatory          | 'Free form'                    |
| startedTime                               | The timestamp of the creation of the user answers                 | Timestamp           | Mandatory          | value inside $date.$numberLong |
| lastUpdated                               | The timestamp of the last update                                  | Timestamp           | Mandatory          | value inside $date.$numberLong |
| validUntil                                | The timestamp of the validity expiry                              | Timestamp           | Mandatory          | value inside $date.$numberLong |

**Required Request Headers**:

| Header Name   | Header Value   | Description                                |
|---------------|----------------|--------------------------------------------|
| Authorization | Bearer {TOKEN} | A valid bearer token from the auth service |

## Responses

### Success response

**Code**: `200 OK`

**Response Body**

The response body returns the user answers entry if set up which relates to details for a return for a specific appaId.

The data section contains the actual user answers. The content will depend on the specific questions answered and will
not be documented in detail here.

| Field Name                                | Description                                                       | Data Type           | Mandatory/Optional | Notes                          |
|-------------------------------------------|-------------------------------------------------------------------|---------------------|--------------------|--------------------------------|
| appaId                                    | The appaId                                                        | String              | Mandatory          |                                |
| userId                                    | The user's id                                                     | String              | Mandatory          |                                |
| subscriptionSummary                       | The user's existing contact preferences from the subscription API | SubscriptionSummary | Mandatory          |                                |
| subscriptionSummary.paperlessReference    | Whether the user is currently on email                            | Boolean             | Mandatory          |                                |
| subscriptionSummary.emailAddress          | The user's existing email address in ETMP                         | String              | Optional           | Encrypted in the backend       |
| subscriptionSummary.emailVerification     | Whether the user's existing email address is verified             | Boolean             | Optional           |                                |
| subscriptionSummary.bouncedEmail          | Whether emails to the user's existing email address have bounced  | Boolean             | Optional           |                                |
| subscriptionSummary.correspondenceAddress | The user's correspondence address (lines 1-4 and postcode)        | String              | Mandatory          |                                |
| subscriptionSummary.countryCode           | The country code of the user's correspondence address             | String              | Optional           |                                |
| emailAddress                              | The email address submitted by the user                           | String              | Optional           | Encrypted in the backend       |
| verifiedEmailAddresses                    | The email addresses that are already verified for the user        | Set(String)         | Mandatory          | Encrypted in the backend       | 
| data                                      | The user answers data                                             | Object              | Mandatory          | 'Free form'                    |
| startedTime                               | The timestamp of the creation of the user answers                 | Timestamp           | Mandatory          | value inside $date.$numberLong |
| lastUpdated                               | The timestamp of the last update                                  | Timestamp           | Mandatory          | value inside $date.$numberLong |
| validUntil                                | The timestamp of the validity expiry                              | Timestamp           | Mandatory          | value inside $date.$numberLong |

**Request/Response Body Examples**

***An example entry with the rest of the data portion removed:***

```json
{
  "appaId": "XMADP1002100211",
  "userId": "Int-01234567-89ab-cdef-fdec-ba9876543210",
  "subscriptionSummary": {
    "paperlessReference": false,
    "emailAddress": "john.doe@example.com",
    "emailVerification": true,
    "bouncedEmail": false,
    "correspondenceAddress": "Flat 123\n1 Example Road\nLondon\nAB1 2CD",
    "countryCode": "GB"
  },
  "emailAddress": "jane.doe@example.com",
  "verifiedEmailAddresses": [
    "john.doe@example.com"
  ],
  "data": {
    "contactPreferenceEmail": true,
    "useExistingEmail": false
  },
  "startedTime": {
    "$date": {
      "$numberLong": "1726378927221"
    }
  },
  "lastUpdated": {
    "$date": {
      "$numberLong": "1726578927221"
    }
  },
  "validUntil": {
    "$date": {
      "$numberLong": "1729170927221"
    }
  }
}
```

### Error responses

**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any consumer without an authorized session that has an ADR enrolment.

**Code**: `404 NOT_FOUND`
No user answers entry to update was found for the appaId

**Code**: `500 INTERNAL_SERVER_ERROR`
This response can occur if the query to the database fails

# API documentation

API: Application Programming Interface

The app, in addition to the Common Voice API (official of Mozilla), use also "CV Android" API (better "Saverio Morelli API").

In this file there will be listed just the second one.

First of all you need to know that, all this API doesn't collect your personal information, like *name*, *last name*, *email*, *age*.

Please, consider to **don't** insert useless data, because these are just to understand how many users use the app and how use it.

## App statistics

### GET

#### Request

To get app statistics you need to do a `GET request` to https://www.saveriomorelli.com/api/common-voice-android/v2/app-statistics/.

If you don't insert any parameters, the system will return all statistics (of the day) about all languages.

You can specify these parameters:

| Key        | Value                       | Required   | Explanation                                                  |
| ---------- | --------------------------- | ---------- | ------------------------------------------------------------ |
| `language` | `all` or the language code* | `required` | *the language code have to be in the "Common Voice style". For example, if you want to get statistics for the Italian language you need to specify the code `it`.<br />If you don't specify anything, the system will return to you statistics of all languages. |

#### Response

The system return a `JSON` data, that contains data like this:

`{"language":{"user":N1,"logged:"M1},"language2":{"user":N2,"logged":M2}}`, where `language` and `language2` are the language codes, `N1`, `N2`, `M1`, `M2` are integers.

So, for each language this are the information:

| Key      | Value     | Explanation                                                  |
| -------- | --------- | ------------------------------------------------------------ |
| `user`   | *Integer* | It indicates the number of users which used the app (just those users who turned on the "App statistics") |
| `logged` | *Integer* | It indicates how many users used the app **and** were logged in (just users who turned on the "App statistics") |

### POST

#### Request

To insert data you need to do a `POST request` to https://www.saveriomorelli.com/api/common-voice-android/v2/.

You need to do a `JSON` request with these data:

| Key        | Value                        | Required   | Explanation                                                  |
| ---------- | ---------------------------- | ---------- | ------------------------------------------------------------ |
| `logged`   | `0` or `1`                   | `required` | It's an integer value `0` if you use the app "anonymously" (without log-in), `1` if you are logged in |
| `username` | *String*                     | `required` | It's a unique string generated just the first time you run the app (not every time you run it), and it doesn't contain personal data.<br />The string is like this: `UserYYYYMMDDHHMMSSMMMM::CVAppSav` |
| `language` | *String*                     | `required` | It's the language code you are using the app (`en`, `it`, ...) |
| `public`   | `true` or `false`            | `required` | It's a flag: `true` if the statistics are public, so are shown in the graph, `false` if you have turned off the statistics |
| `version`  | *Integer*                    | `required` | It's the version code of the app (`90`, `91`, ...)           |
| `source`   | `GPS` or `FD-GH` (or `n.d.`) | `optional` | It indicates the source from you installed the app (GPS: Google Play Store, FD-GH: F-Droid/GitHub) |

#### Response

The `POST request` could returns these status code:

| Code  | Type    | Description                            | Explanation and possible fix                                 |
| ----- | ------- | -------------------------------------- | ------------------------------------------------------------ |
| `200` | `OK`    | `Record inserted correctly.`           | Data are valid and they are been inserted correctly          |
| `400` | `Error` | `Record has already inserted today.`   | Invalid because data are already inserted today              |
| `401` | `Error` | `Something was wrong in POST request.` | DB rejected the data, so try again or check them             |
| `402` | `Error` | `Parameters are wrong, check them.`    | Parameters passed are incorrect or not enough                |
| `403` | `Error` | `Can't insert record on database.`     | DB rejected the data, so try again or check data passed      |
| `500` | `Error` | `Can't connect to the database.`       | It's unavailable the connection with the database in that moment |



## Logs collection

### GET

#### Request

To get app statistics you need to do a `GET request` to https://www.saveriomorelli.com/api/common-voice-android/v2/logs/get/.

#### Response

### POST

#### Request

To insert data you need to do a POST request to https://www.saveriomorelli.com/api/common-voice-android/v2/logs/.

 #### Response

## App usage

### GET

#### Request

To get app statistics you need to do a `GET request` to https://www.saveriomorelli.com/api/common-voice-android/v2/app-usage/get/.

#### Response

### POST

#### Request

To insert data you need to do a POST request to https://www.saveriomorelli.com/api/common-voice-android/v2/app-usage/.

#### Response
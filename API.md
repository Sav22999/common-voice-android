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
| `language` | `all` or the language code* | `optional` | *the language code have to be in the "Common Voice style". For example, if you want to get statistics for the Italian language you need to specify the code `it`.<br />If you don't specify anything, the system will return to you statistics of all languages. |

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

You need to send a `JSON` request with these data:

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

You can specify these parameters:

| Key     | Value     | Required   | Explanation                                                  |
| ------- | --------- | ---------- | ------------------------------------------------------------ |
| `limit` | *Integer* | `optional` | You can specify an *Integer* which indicates the number of records (logs) you want to see.<br />If you'd like the last 100 logs, for example, you should specify `100`.<br />If you don't specify anything, it's `100` as default. The number have to be in 1 and 1000 interval. |

#### Response

The system returns a `JSON` file, which has a counter from `1` , and it cointains are two big section: `general` and `log`.

`general`:

| Key        | Value      | Explanation                                                  |
| ---------- | ---------- | ------------------------------------------------------------ |
| `id`       | *Integer*  | It's the id of the log in the database                       |
| `date`     | *DateTime* | It's the date when the log was added in the system           |
| `language` | *String*   | It's the language code you are using the app (`en`, `it`, ...) |
| `version`  | *Integer*  | It's the version code of the app (`90`, `91`, ...)           |
| `source`   | *String*   | It indicates the source from you installed the app (GPS: Google Play Store, FD-GH: F-Droid/GitHub) |
| `logged`   | `0` or `1` | It's an integer value `0` if you use the app "anonymously" (without log-in), `1` if you are logged in |

`log`:

| Key              | Value    | Explanation                                                  |
| ---------------- | -------- | ------------------------------------------------------------ |
| `errorLevel`     | *String* | It indicates shortly which type of message it is (like `Error`, `Warning`, etc.) |
| `tag`            | *Text*   | It's a string which indicates the class name where the error happened |
| `stackTrace`     | *Text*   | It's the description of the error                            |
| `additionalLogs` | *Text*   | `optional` \It's omre information (context) about the error. This field is not required. |



### POST

#### Request

To insert data you need to do a POST request to https://www.saveriomorelli.com/api/common-voice-android/v2/logs/.

| Key              | Value            | Required   | Explanation |
| ---------------- | ---------------- | ---------- | ----------- |
| `logged`         | `0` or `1`       | `required` |             |
| `language`       | *String*         | `required` |             |
| `version`        | *Integer*        | `required` |             |
| `source`         | `GPS` or `FD-GH` | `required` |             |
| `errorLevel`     | *String*         | `required` |             |
| `tag`            | *Text*           | `required` |             |
| `stackTrace`     | *Text*           | `required` |             |
| `additionalLogs` | *Text*           | `optional` |             |



 #### Response

| Code  | Type    | Description                        | Explanation and possible fix                                 |
| ----- | ------- | ---------------------------------- | ------------------------------------------------------------ |
| `200` | `OK`    | `Record inserted correctly.`       | Record inserted correctly in the database                    |
| `400` | `Error` | `Can't insert record on database.` | DB rejected the data, so try again or check data passed      |
| `401` | `Error` | `Parameters are not enough.`       | Parameters are not enough or they are too much.              |
| `500` | `Error` | `Can't connect to the database.`   | It's unavailable the connection with the database in that moment |



## App usage

### GET

#### Request

To get app statistics you need to do a `GET request` to https://www.saveriomorelli.com/api/common-voice-android/v2/app-usage/get/.

You can insert these parameters to the `GET` request:

| Key        | Value    | Required   | Explanation                                                  |
| ---------- | -------- | ---------- | ------------------------------------------------------------ |
| `language` | *String* | `optional` | It's the language code of the language you want to see. You can specify also `all`, the system will return all languages.<br />If you don't specify anything, the system will return all languages |
| `year`     | *String* | `optional` | It indicates the year (format `YYYY`). If you specify this, the contributions will be relative of all that year, otherwise the system will display to you just the contributions of today.<br />If you want the contributions of ever, you can specify `always`. |

#### Response

You will get a which has as key the *language code* of the language selected (or all languages). This key contains other two big section: `listen` and `speak`, these contain other fields.

`listen`:

| Key         | Value     | Explanation                                         |
| ----------- | --------- | --------------------------------------------------- |
| `validated` | *Integer* | Indicates all clips validated (both "Yes" and "No") |
| `accepted`  | *Integer* | Indicates all clips you accepted ("Yes")            |
| `rejected`  | *Integer* | Indicates all clips you rejected ("No")             |
| `reported`  | *Integer* | Indicates all clips you reported ("Report")         |

`speak`:

| Key        | Value     | Explanation                                                  |
| ---------- | --------- | ------------------------------------------------------------ |
| `sent`     | *Integer* | Indicates all sentences you recorded and you sent the recording to the Common Voice server |
| `reported` | *Integer* | Indicates all sentences you reported ("Report")              |

### POST

#### Request

To insert data you need to do a POST request to https://www.saveriomorelli.com/api/common-voice-android/v2/app-usage/.

| Key        | Value                           | Required   | Explanation                                                  |
| ---------- | ------------------------------- | ---------- | ------------------------------------------------------------ |
| `logged`   | `0` or `1`                      | `required` | If the user is logged in, so it's `1`, otherwise it's `0`    |
| `language` | *String*                        | `required` | It's the language code, like `it`, `en`, etc.                |
| `version`  | *Integer*                       | `required` | It's the version code of the app                             |
| `source`   | `GPS` or `FD-GH`                | `required` | It's the app store/source from that you downloaded and installed the app |
| `type`     | `0` or `1` or `2` or `3` or `4` | `required` | It's an integer value `0 ` or `1` if you validated a clip (the first one "rejected", the latter one "accepted"), `2` if you reported a clip, `3` if you sent a recording and `4` if you reported a sentence.<br />So, the `0`, `1` and `2` are about "Listen", the `3` and `4` are about "Speak" |
| `username` | *String*                        | `required` | It's a unique string generated just the first time you run the app (not every time you run it), and it doesn't contain personal data.<br />The string is like this: `UserYYYYMMDDHHMMSSMMMM::CVAppSav` |
| `offline`  | `0` or `1`                      | `required` | It indicates if you are using the app in `offline` (`1`) mode (without any connection) or `online` (`0`) |



#### Response

| Code  | Type    | Description                        | Explanation and possible fix                                 |
| ----- | ------- | ---------------------------------- | ------------------------------------------------------------ |
| `200` | `OK`    | `Record inserted correctly.`       | Record inserted correctly in the database                    |
| `400` | `Error` | `Can't insert record on database.` | DB rejected the data, so try again or check data passed      |
| `401` | `Error` | `Parameters are not enough.`       | Parameters are not enough or they are too much.              |
| `500` | `Error` | `Can't connect to the database.`   | It's unavailable the connection with the database in that moment |


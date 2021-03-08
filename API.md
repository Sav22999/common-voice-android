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

| Key        | Value                               | Required   | Explanation                                                  |
| ---------- | ----------------------------------- | ---------- | ------------------------------------------------------------ |
| `logged`   | `0` or `1`                          | `required` | It's an integer value `0` if you use the app "anonymously" (without log-in), `1` if you are logged in |
| `username` | *String*                            | `required` | It's a unique string generated just the first time you run the app (not every time you run it), and it doesn't contain personal data.<br />The string is like this: `UserYYYYMMDDHHMMSSMMMM::CVAppSav` |
| `language` | *String*                            | `required` | It's the language code you are using the app (`en`, `it`, ...) |
| `public`   | `true` or `false`                   | `required` | It's a flag: `true` if the statistics are public, so are shown in the graph, `false` if you have turned off the statistics |
| `version`  | *Integer*                           | `required` | It's the version code of the app (`90`, `91`, ...)           |
| `source`   | `GPS`, `FD-GH` or `HAG` (or `n.d.`) | `optional` | It indicates the source from you installed the app (`GPS`: Google Play Store, `FD-GH`: F-Droid/GitHub, `HAG`: Huawei AppGallery, `n.d.`: not defined) |

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

The system returns a `JSON` file, which has a counter from `1` , and it contains are two big section: `general` and `log`.

`general`:

| Key        | Value      | Explanation                                                  |
| ---------- | ---------- | ------------------------------------------------------------ |
| `id`       | *Integer*  | It's the id of the log in the database                       |
| `logDate`  | *DateTime* | It's the data when the log was verified.                     |
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

| Key              | Value                               | Required   | Explanation                                                  |
| ---------------- | ----------------------------------- | ---------- | ------------------------------------------------------------ |
| `logDate`        | *DateTime*                          | `required` | It's the datetime when the issue is verified. This could be different from the datetime when the log arrives in the database.<br>The format of this data should be: `YYYY-MM-DD HH:MM:SS`. |
| `logged`         | `0` or `1`                          | `required` | It's an integer value `0` if you use the app "anonymously" (without log-in), `1` if you are logged in |
| `language`       | *String*                            | `required` | It's the language code you are using the app (`en`, `it`, ...) |
| `version`        | *Integer*                           | `required` | It's the version code of the app (`90`, `91`, ...)           |
| `source`         | `GPS`, `FD-GH` or `HAG` (or `n.d.`) | `required` | It indicates the source from you installed the app (`GPS`: Google Play Store, `FD-GH`: F-Droid/GitHub, `HAG`: Huawei AppGallery, `n.d.`: not defined) |
| `errorLevel`     | *String*                            | `required` | It's string which indicates the error level, like `Info`, `Error`, `Warning`, etc. |
| `tag`            | *Text*                              | `optional` | It's a string which indicates the class name where the error happened |
| `stackTrace`     | *Text*                              | `required` | It's the description of the error                            |
| `additionalLogs` | *Text*                              | `optional` | It's more information (context) about the error. This field is not required. |



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

| Key        | Value     | Required   | Explanation                                                  |
| ---------- | --------- | ---------- | ------------------------------------------------------------ |
| `language` | *String*  | `optional` | It's the language code of the language you want to see. You can specify also `all`, the system will return all languages.<br />If you don't specify anything, the system will return all languages |
| `filter`   | *String*  | `optional` | It filters the result of contributions, you can set to `today` (contributions of today), `yesterday` (contributions of yesterday), `always` (contributions of always) or `year` (contributions of year and you have to specify the `year` parameter too). |
| `year`     | *Integer* | `optional` | Specify it just if you specified `filter`=`year`.<br />You need to specify the year you want to see. If you don't specify anything, it's set automatically to `filter`=`today`. |

The `language` can also be `all`.

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
| `source`   | `GPS`, `FD-GH`, `HAG`           | `required` | It's the app store/source from that you downloaded and installed the app |
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

## App usage for a user

### GET

#### Request

To get app statistics you need to do a `GET request` to https://www.saveriomorelli.com/api/common-voice-android/v2/app-usage/get/user/.

You can insert these parameters to the `GET` request:

| Key          | Value                 | Required   | Explanation                                                  |
| ------------ | --------------------- | ---------- | ------------------------------------------------------------ |
| `id`         | *String*              | `required` | It's the username* of the user you want to see the statistics. You can find your username in `Settings | Advanced | Show the string which identify me inside the app `. |
| `start_date` | *Date* (`YYYY-MM-DD`) | `optional` | It indicates the date since ("**from**") which you want to see the statistics. Its format have to be `YYYY-MM-DD`.<br />If you want the contributions of ever, you can specify `always`.<br/>If you want the contributions of today, you can specify `today`.<br/>If you don't specify anything, the system automatically will show you statistics of ever. |
| `end_date`   | *Date* (`YYYY-MM-DD`) | `optional` | It indicates the date until ("**to**") which you want to see the statistics. Its format have to be `YYYY-MM-DD`.<br />If you want the contributions of today, you can specify `today`.<br/>If you don't specify anything, the system automatically will show you statistics of today. |

**It's the CV Android username of the user. It's anonymous and if you clear data of the app, the username changes.*

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

## App usage (detailed)

### GET

#### Request

To get app statistics you need to do a `GET request` to https://www.saveriomorelli.com/api/common-voice-android/v2/app-usage/get/details/.

You can insert these parameters to the `GET` request:

| Key        | Value    | Required   | Explanation                                                  |
| ---------- | -------- | ---------- | ------------------------------------------------------------ |
| `language` | *String* | `optional` | It's the language code of the language you want to see. You can specify also `all`, the system will return all languages.<br />If you don't specify anything, the system will return all languages |
| `year`     | *String* | `optional` | It indicates the year (format `YYYY`). If you specify this, the contributions will be relative of all that year, otherwise the system will display to you just the contributions of today.<br />If you want the contributions of ever, you can specify `always`. |

The `language` can also be `all`.

#### Response

You will get a which has as key the *language code* of the language selected (or all languages). This key contains other two big section: `listen` and `speak`. These sections contain other subsections: (for *listen*:) `validated`, `accepted`, `rejected` and `reported` and (for *speak*:) `sent` and `reported`.

The `validated` for Listen are the sum of `accepted`, `rejected` and `reported`. For more details about those subsections, please see *App usage* above.

Every subsection has these fields:

| Key         | Value     | Explanation                                                  |
| ------------- | ----------- | -------------------------------------------------------- |
| `all`       | *Integer* | Indicates all clips/sentences/recordings (so it's the sum of `online`+`offline` **or** `logged`+`no-logged`) |
| `online`    | *Integer* | Indicates all clips/sentences/recordings made in the app **online** (so with an Internet connection) |
| `offline`   | *Integer* | Indicates all clips/sentences/recordings made in the app **offline** (so without an Internet connection) |
| `logged`    | *Integer* | Indicates all clips/sentences/recordings made in the app **logged in** (so log in to your account) |
| `no-logged` | *Integer* | Indicates all clips/sentences/recordings made in the app **no-logged in** (so you don't log in to your account) |

## In-app messages

### GET

#### Request

To get in-app messages you need to do a `GET request` to https://www.saveriomorelli.com/api/common-voice-android/v2/messages/get/.

You can insert these parameters to the `GET` request:

| Key  | Value     | Required   | Explanation                                                  |
| ---- | --------- | ---------- | ------------------------------------------------------------ |
| `id` | *Integer* | `optional` | You can specify a message_id, otherwise the system will return to you all messages |

#### Response

If you specified the id, you will get just one result, so `1` which contains these parameters. Otherwise, you will get more results (`1`,`2`,...), in sorted by `message_id DESC`.

| Key           | Value             | Explanation                                                  |
| ------------- | ----------------- | ------------------------------------------------------------ |
| `id`          | *Integer*         | It's the message id                                          |
| `type`        | *Integer*\|`NULL` | It indicates the message type. If it's different to , `NULL`, the app will open a popup instead of a banner. Popup supported are: `1` (standard), `5` (info), `6` (help), `7` (warning), `8` (news/changelog),`9` (tip).<br />`NULL` (default value) means it's a **banner**. |
| `user`        | *String*\|`NULL`  | If this field is not `NULL`, so it means `versionCode`, `language` and `source` are ignored.<br />This field indicates the userid (`::CVAppSav`) of the user you want to send a message. |
| `versionCode` | *Integer*\|`NULL` | It indicates the goal app version code.<br />`NULL` (default value) means the message is for all version codes. |
| `language`    | *String\|`NULL`*  | It's the goal language for the message (language code supported in the app).<br />`NULL` (default value) means the message is for all languages. |
| `source`      | *String*\|`NULL`  | It indicates the store where the app has been installed (Google Play, F-Droid, etc.).<br />`NULL` (default value) means the message is for all stores (`GPS`, `FD-GH`, `HAG`). |
| `startDate`   | *Date*\|`NULL`    | It indicates the start date the message is valid.<br />`NULL` (default value) means there isn't a specified start date ("from"). |
| `endDate`     | *Date*\|`NULL`    | It indicates the end date the message is valid.<br />`NULL` (default value) means there isn't a specified end date ("until"). |
| `text`        | *Text*            | It's the actual text the banner show contains.               |
| `ableToClose` | *Boolean*         | If `true` it means the message can be closed (with the button "X"), otherwise the "X" button is hidden. |
| `button1`     | *Text*\|`NULL`    | It's the text of the first button.<br />`NULL` (default value) means buttons are not required for this message. |
| `button1Link` | *Text*            | It's the link of the first button.<br />If `button1` is not `NULL`, the link is required. |
| `button2`     | *Text\|`NULL`*    | It's the text of the second button.<br />`NULL` (default value) means the second button is not required for this message. |
| `button2Link` | *Text*            | It's the link of the second  button.<br />If `button2` is not `NULL`, the link is required. |

## Languages

### GET

#### Request

To get in-app messages you need to do a `GET request` to https://www.saveriomorelli.com/api/common-voice-android/v2/languages/.

#### Response

You will get all supported languages in the app. For each language (the code) you will get also these details:

| Key          | Value     | Explanation                                                  |
| ------------ | --------- | ------------------------------------------------------------ |
| `native`     | *String*  | It's the native name of the language                         |
| `english`    | *String*  | It's the name of the language in English                     |
| `crowdin`    | *Boolean* | Indicates the language exists or not on Crowdin (`false`: not exists/not supported, `true`: supported) |
| `percentage` | *Integer* | It's the translation percentage of the app (on Crowdin). If the language is not present of Crowdin, the percentage is 0 |

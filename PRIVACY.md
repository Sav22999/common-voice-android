

#  <img src="images/icon.png" width="40px" alt=""></img> Privacy: CV Android

The app doesn’t collect your personal data. Anyway, some data are saved on your device (*user_id* of Common Voice, *selected language*, *validations and recordings number*, and others data). You can clear them just *Clear data* of the app (but they are necessary to use Common Voice Android).

The app requires *Storage* and *Microphone* permissions when you use *Speak section*: because the app saves your recordings, and the app deletes automatically them (after sent to the Common Voice server). The app will ask permissions just when it needs of them.

If you turn on "Save logs to a file", the app requires *Storage* and will be save logs to files, but they won't contain your personal data. Read more about this in *Saving the logs* section (below).

The *Experimental features* could be unsafe and unstable, so pay attention if you use that option.

There are "two" versions: one for "Google Play Store" (GPS), and another for "F-Droid/GitHub" (FD-GH). In the first one the app show, sometimes a message "Review the app ... on Google Play Store", in the second one there is a button, in Settings, which permits you to "buy me a coffee". But the rest of the code is absolutely the same.

If you want additional information about the Policy privacy of Common Voice project, [read the official doc by Mozilla](https://commonvoice.mozilla.org/en/privacy).

**All statistics** are anonymous. CV Android nor Saverio Morelli collect your personal data. The statistics are useful just to see how many users use the app and how many contributions are done in the app.

### Generic statistics

*You can disable Generic statistics always, in Settings. If you disable these statistics, you will not be in public statistics, but app sent anyway data to my database.*

These statistics are absolutely anonymous. I don't collect your personal data. The first time (per day) you run the app, they send: **unique_id**, **language**, **logged_status**, **version** and **status_statistics**.

| Key         | Value             | Explanation                                                  |
| ----------- | ----------------- | ------------------------------------------------------------ |
| `unique_id` | *String*          | It's a unique string generated just the first time you run the app (not every time you run it), and it doesn't contain personal data.<br />The string is like this: `UserYYYYMMDDHHMMSSMMMM::CVAppSav` |
| `language`  | *String*          | It's the language code you are using the app (`en`, `it`, ...) |
| `logged`    | `0` or `1`        | It's an integer value `0` if you use the app "anonymously" (without log-in), `1` if you are logged in |
| `version`   | *Integer*         | It's the version code of the app (`90`, `91`, ...)           |
| `public`    | `true` or `false` | It's a flag: `true` if the statistics are public, so are shown in the graph, `false` if you have turned off the statistics |
| `source`    | `GPS` or `FD-GH`  | It indicates the source from you installed the app (GPS: Google Play Store, FD-GH: F-Droid/GitHub) |

You can see public statistics on website: [https://saveriomorelli.com/app/common-voice-android/statistics](https://bit.ly/35d2dza).

### Saving the logs

*This option may slow down app performance, so turn on just when it's necessary.*

*You can turn on/off in Settings.*

The file log is saved in an internal database, to a file and, also, it's sent to my website. In this way I can see fast issues and fix them. This is what I send to my website:

| Key              | Value            | Explanation                                                  |
| ---------------- | ---------------- | ------------------------------------------------------------ |
| `logDate`        | *DateTime*       | It's the datetime when the issue is verified. This could be different from the datetime when the log arrives in the database.<br/>The format of this data should be: `YYYY-MM-DD HH:MM:SS`. |
| `logged`         | `0` or `1`       | It's an integer value `0` if you use the app "anonymously" (without log-in), `1` if you are logged in |
| `language`       | *String*         | It's the language code you are using the app (`en`, `it`, ...) |
| `version`        | *Integer*        | It's the version code of the app (`90`, `91`, ...)           |
| `source`         | `GPS` or `FD-GH` | It indicates the source from you installed the app (GPS: Google Play Store, FD-GH: F-Droid/GitHub) |
| `errorLevel`     | *String*         | It's string which indicates the error level, like `Info`, `Error`, `Warning`, etc. |
| `tag`            | *Text*           | `optional` \| It's a string which indicates the class name where the error happened |
| `stackTrace`     | *Text*           | It's the description of the error                            |
| `additionalLogs` | *Text*           | `optional` \| It's more information (context) about the error. This field is not required. |

**This feature needs "Storage" permission.**

Logs are saved in the `common-voice-android.log` file, so in that file will be all exception, warning or error messages. The file will contain max 1000 rows.

Your data, of course, will **not** be saved. <u>The logs file will contain messages about the app, not about you.</u>

This option is useful when you want to report a bug, so you can attach this file and the developer can see more details.

### App usage statistics

The app, from version 2.2, send to my website also the app usage. These information are absolutely anonymous and are useful to see how many users use, actually, the app and how many contributions are from the app.

*You can disable App usage statistics in Settings. If you disable them, the app anyway will send query to my server, but the username is empty, in this way there is no way to know from who arrives statistics.* 

This is what the app send to my server:

| Key        | Value                           | Explanation                                                  |
| ---------- | ------------------------------- | ------------------------------------------------------------ |
| `type`     | `0` or `1` or `2` or `3` or `4` | It's an integer value `0 ` or `1` if you validated a clip (the first one "rejected", the latter one "accepted"), `2` if you reported a clip, `3` if you sent a recording and `4` if you reported a sentence.<br />So, the `0`, `1` and `2` are about "Listen", the `3` and `4` are about "Speak" |
| `language` | *String*                        | It's the language code you are using the app (`en`, `it`, ...) |
| `version`  | *Integer*                       | It's the version code of the app (`90`, `91`, ...)           |
| `source`   | `GPS` or `FD-GH`                | It indicates the source from you installed the app (GPS: Google Play Store, FD-GH: F-Droid/GitHub) |
| `logged`   | `0` or `1`                      | It's an integer value `0` if you use the app "anonymously" (without log-in), `1` if you are logged in |
| `offline`  | `0` or `1`                      | It's an integer value `0` if you were using the app online when you contributed to Common Voice with the app, `1` if you were using it offline<br />It's useful to know if users use the offline mode or not |
| `username` | *String*                        | It's a unique string generated just the first time you run the app (not every time you run it), and it doesn't contain personal data.<br />The string is like this: `UserYYYYMMDDHHMMSSMMMM::CVAppSav` |



*Last update: 30th November 2020*
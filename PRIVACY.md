

#  <img src="images/icon.png" width="40px" alt=""></img> Privacy: Common Voice Android

The app doesn’t collect your personal data. Anyway, some data are saved on your device (*user_id* of Common Voice, *selected language*, *validations and recordings number*, and others data). You can clear them just *Clear data* of the app (but they are necessary to use Common Voice Android).

The app requires *Storage* and *Microphone* permissions when you use *Speak section*: because the app saves your recordings, and the app deletes automatically them (after sent to the Common Voice server).

If you turn on "Save logs to a file", the app requires *Storage* and will be save logs to files, but they won't contain your personal data. Read more about this in *Saving the logs* section (below).

The *Experimental features* could be unsafe and unstable, so pay attention if you use that option.

There are "two" versions: one for "Google Play Store", and another for "F-Droid/GitHub". In the first one the app show, sometimes a message "Review the app ... on Google Play Store", in the second one there is a button, in Settings, which permits you to "buy me a coffee". But the rest of the code is absolutely the same.

If you want additional information about the Policy privacy of Common Voice project, [read the official doc](https://commonvoice.mozilla.org/en/privacy).

### Anonymous statistics

*You can disable Anonymous Statistics always, in Settings. If you disable statistics, you will not be in public statistics, but app sent anyway data to my database.*

These statistics are absolutely anonymous. I don't collect your personal data. The first time (per day) you run the app, they send: **unique_id**, **language**, **logged_status**, **version** and **status_statistics**.

| Key         | Value                              | Explanation                                                  |
| ----------- | ---------------------------------- | ------------------------------------------------------------ |
| `unique_id` | `UserYYYYMMDDHHMMSSMMMM::CVAppSav` | It's a unique string generated just the first time you run the app (not every time you run it), and it doesn't contain personal data |
| `language`  | `en`, `it`, _ect._ (or `n.d.`)     | It's the language code you are using the app                 |
| `logged`    | `0` or `1`                         | It's an integer value `0` if you use the app "anonymously" (without log-in), `1` if you are logged in |
| `version`   | `98`,`100`, *ect.* (or `n.d.`)     | It's the version code of the app                             |
| `public`    | `true` or `false`                  | It's a flag: `true` if the statistics are public, so are shown in the graph, `false` if you have turned off the statistics |
| `source`    | `GPS` or `FD-GH` (or also `n.d.`)  | It indicates the source from you installed the app (GPS: Google Play Store, FD-GH: F-Droid/GitHub) |

You can see public statistics on website: [https://saveriomorelli.com/app/common-voice-android/statistics](https://bit.ly/35d2dza).

### Saving the logs

*This option may slow down app performance, so turn on just when it's necessary.*

*You can turn on/off in Settings.*

**This feature needs "Storage" permission.**

Logs are saved in the `common-voice-android.log` file, so in that file will be all exception, warning or error messages. The file will contain max 1000 rows.

Your data, of course, will **not** be saved. <u>The logs file will contain messages about the app, not about you.</u>

This option is useful when you want to report a bug, so you can attach this file and the developer can see more details.



*Last update: 15th May 2020*
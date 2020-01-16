

#  <img src="images/icon.png" width="40px" alt=""></img> Common Voice 

This is not the official app of the project Common Voice by Mozilla. This app is developed by Saverio Morelli, via Android Studio, because of there isn’t an official app for Android.

[![Crowdin](https://badges.crowdin.net/common-voice-android/localized.svg)](https://crowdin.com/project/common-voice-android) [![Generic badge](https://img.shields.io/badge/supported%20languages-6-green.svg)](https://crowdin.com/project/common-voice-android) [![GitHub release](https://img.shields.io/github/release/Sav22999/common-voice-android.svg)](https://github.com/Sav22999/common-voice-android/releases/) [![GitHub license](https://img.shields.io/github/license/Sav22999/common-voice-android.svg)](https://github.com/Sav22999/common-voice-android/blob/master/LICENSE) [![Github all releases](https://img.shields.io/github/downloads/Sav22999/common-voice-android/total.svg)](https://GitHub.com/Sav22999/common-voice-android/releases/) ![Maintenance](https://img.shields.io/badge/Maintained%3F-yes-green.svg)

[<img src="images/googlePlayBadge.png" width="200px"></img>](https://play.google.com/store/apps/details?id=org.commonvoice.saverio) [<img src="images/aptoideBadge.png" height="50px"></img>](https://common-voice-android.en.aptoide.com/?store_name=sav22999) [<img src="images/crowdinBadge.png" height="50px"></img>](https://crowdin.com/project/common-voice-android)

If you have any questions, please open an issue.

You can contact me also on Telegram, with the username `@Sav22999`.

### How contribute

If you want to help to develop this app, you can open an `Issue` an send feedback about the features or bugs. In the `screenshots` folder there are updated screenshots of the app status.

You can contribute also to translate the app:

- **Open a new issue** (use `Translation` template), so others know you are translating the app in that language
- Then go to [Crowdin](https://crowdin.com/project/common-voice-android), choose your language (you can ask for a new language if it’s not in the list) and translate strings.

#### To do:

- **PRIORITY**: Create `POST` request to send the recording (Now it saves the audio on device, but I don’t know how to send the audio file to CV server) -> to do tests use voice.allizom.org instead of the main server -> You should send as `opus codecs` (probably)
- **PRIORITY**: Statistics `You` for `Today` (I don’t know how to get these information)
- Improve the loading of sentences/clips (download `2` clips per time. When you finish to validate the `1st`: `firstClip`=`secondClip`, `secondClip`=`loadNewClip()`)

### Screenshots

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="200px"></img><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="200px"></img><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="200px"></img><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width="200px"></img><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" width="200px"></img><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/6.png" width="200px"></img><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/7.png" width="200px"></img><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/8.png" width="200px"></img>

If you want screenshots of the other versions, go to the `screenshots` folder.

### Why an Android app of Common Voice?

There is already the website, which is response and mobile-friendly, but actually it’s very slow, because of so many effects, which could distract you to record or validate clips. So, this app want to improve the experience of this fantastic project, you can fiend you statistics, or you can record/validate a clip just with a single click.

### Translators

The app is officially translated in these following languages, **thank you very much** to the contributors who translated the app.

- `en` English (main language)
- `eu` Basque -> **[Mielanjel Iraeta](https://crowdin.com/profile/pospolos)**
- `ia` Interlingua -> **[Carmelo Serraino](https://crowdin.com/profile/Melo46)**
- `it` Italian -> **[Saverio Morelli](https://github.com/Sav22999)**
- `sv-SE` Swedish -> **[Linus Amvall](https://github.com/klasrocket)**
- `fr` French -> **[PoorPockets McNewHold](https://crowdin.com/profile/IfiwFR)**

### License

The license of *Common Voice Android* is GPLv3.

<img src="images/gpl.png" width="100px"></img>



![Generic badge](https://img.shields.io/badge/built%20in-Android%20Studio-green.svg) ![Generic badge](https://img.shields.io/badge/developed%20in-Kotlin-blue.svg)

![GitHub last commit](https://img.shields.io/github/last-commit/Sav22999/common-voice-android) [![Generic badge](https://img.shields.io/badge/developed%20by-Sav22999-lightgrey.svg)](https://saveriomorelli.com)
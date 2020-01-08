# Common Voice Android

This is not the official app of the project Common Voice by Mozilla. This app is developed by Saverio Morelli, via Android Studio, because of there isn’t an official app for Android.

If you have any questions, please open an issue.

### How contribute

If you want to help to develop this app, you can open an “Issue” an send feedback about the features or bugs. In the “Screenshots” folder there are update screenshots of the app status and in the “Screenshots”>“0.0.0a (Draft)” you can find the original (initial) idea about the app.

You can contribute also to translate the app, in two modes (the first one is the easiest):

**First mode**:

- Open a new issue (use TRANSLATION template), so others know you are translating the app in that language
- Then go to https://crowdin.com/project/common-voice-android choose your language (you can ask for a new language if it’s not in the list on Crowdin project) and translate strings.

**Second mode**:

- Open an issue and tell us you want to translate in your language (use TRANSLATION template): in this way, others people can know you’re translating the app in that language
- Fork this repo and go to your repo-forked
- Create a branch: “new-languages-<u>**lang**</u>”
- Go to “Languages” and translate in your language the file “strings.xml” (it’s in English)
- Create a new folder “**lang**” and put the *strings.xml* file modified there
- Commit the changes in your repo and create a pull-request

_<u>**lang** is the short-name of your languages ISO639-1 (e.g. Italian: it, English: en, Swedish: sv, ect.)</u>_

#### To do:

- **PRIORITY**: Create POST request to send the recording (Now it saves the audio on device, but I don’t know how to send the audio file to CV server) -> to do tests use voice.allizom.org instead of the main server.
- **PRIORITY**: Statistics "You” for “Today” (I don’t know how to get these information)
- Improve the loading of sentences/clips (download 5s per time, and when you are at the "4th” automatically download others)
- Add option in Settings (switch on/off) to play automatically the new clip after validation/skip (so after loading) in Listen section

### Screenshots

You can find updated screenshots in the “Screenshots” folder.

### Why an Android app of Common Voice?

There is already the website, which is response and mobile-friendly, but actually it’s very slow, because of so many effects, which could distract you to record or validate clips. So, this app want to improve the experience of this fantastic project, you can fiend you statistics, or you can record/validate a clip just with a single click.

### Translators

The app is officially translated in these following languages, **thank you very much** to the contributors who translated the app.

- English -> Saverio Morelli - **[@Sav22999](https://github.com/Sav22999)**
- Italian -> Saverio Morelli - **[@Sav22999](https://github.com/Sav22999)**
- Swedish -> Linus Amvall - **[@klasrocket](https://github.com/klasrocket)**
- French -> **[@PoorPocketsMcNewHold](https://github.com/PoorPocketsMcNewHold)**

### License

The license of *Common Voice Android* is GPLv3.
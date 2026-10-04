# A/D App — GitHub APK Builder

এই ZIP-টি দুইটি সরবরাহ করা PDF-এর ভিত্তিতে তৈরি করা হয়েছে। এটি GitHub Actions দিয়ে APK build করার জন্য প্রস্তুত।

## কী আছে

- App name: **A/D**
- Wi-Fi/network status check
- Wi-Fi না থাকলে network settings খোলার alert
- `Movies/A_D_Downloads/` ফোল্ডারে Android `DownloadManager` দিয়ে sample MP4 download
- Foreground Service এবং সীমিত `WakeLock` support
- Android 13+ notification permission request
- Optional Android ID device lock
- GitHub Actions workflow

## গুরুত্বপূর্ণ build পরিবর্তন

মূল নির্দেশনার workflow-তে ব্যবহৃত পুরোনো action version-এর বদলে বর্তমান GitHub Action major versions ব্যবহার করা হয়েছে। Build-টি wrapper JAR-এর উপর নির্ভর না করে GitHub Actions-এর `setup-gradle` দিয়ে Gradle 8.2.1 সেটআপ করে `gradle assembleDebug` চালায়। ফলে incomplete/missing `gradlew` wrapper-এর কারণে workflow আটকে যাবে না।

## GitHub-এ মোবাইল থেকে APK বানানোর ধাপ

1. GitHub-এ নতুন repository খুলুন।
2. ZIP-টি extract করুন। ভিতরের **A-D-App** ফোল্ডারের সব ফাইল/ফোল্ডার repository-এর root-এ upload করুন। অর্থাৎ repository root-এ `.github`, `app`, `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties` দেখা যাবে।
3. Commit/Save করুন।
4. GitHub repository → **Actions** খুলুন।
5. **Build Android APK** workflow নির্বাচন করুন।
6. **Run workflow** চাপুন।
7. Build সফল হলে workflow run-এর **Artifacts** থেকে **AD-App-APK** download করুন।
8. ZIP extract করলে `app-debug.apk` পাওয়া যাবে।

## Sample download link বদলাতে

`app/src/main/java/com/example/adapp/MainActivity.kt`-এর এই লাইনটি বদলান:

```kotlin
val videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4"
```

শুধু এমন HTTP/HTTPS URL ব্যবহার করুন যেখান থেকে Android DownloadManager সরাসরি ফাইল পেতে পারে।

## Device lock চালু করতে

`MainActivity.kt`-এ:

```kotlin
private val ENFORCE_DEVICE_LOCK = false
private val MY_SECURE_DEVICE_ID = "dummy_id_replace_me"
```

প্রথমে `ENFORCE_DEVICE_LOCK = false` রেখেই অ্যাপ চালিয়ে Android ID দেখুন। তারপর নিজের Android ID বসিয়ে `true` করলে lock সক্রিয় হবে। ভুল ID দিলে অ্যাপ access deny করবে।

## নোট

Foreground Service/WakeLock ব্যাকগ্রাউন্ড কাজের সহায়তা করে, কিন্তু Android-এর battery optimization ও system rules-এর কারণে কোনো অ্যাপকে "কখনোই kill হবে না"—এমন নিশ্চয়তা দেওয়া যায় না।

আরেকটি গুরুত্বপূর্ণ বিষয়: এই project Telegram/অন্য অ্যাপের ভিতর থেকে স্বয়ংক্রিয়ভাবে ভিডিও খুঁজে বের করে না; PDF-এ দেওয়া সীমাবদ্ধতার মতোই এটি নির্দিষ্ট URL-এর sample download করে।

# কর তদন্ত প্রতিবেদন — Android অ্যাপ

ওয়েবসাইটের সব সুবিধা এই অ্যাপেও আছে:
- Excel ও Word ফাইল ইমপোর্ট
- Excel থেকে কপি করা সারি সরাসরি পেস্ট
- ইংরেজি ব্যাংকের নাম ও হিসাবের ধরণ বাংলায় রূপান্তর
- Word ফাইল ডাউনলোড এবং PDF সংরক্ষণ

অ্যাপটি ইন্টারনেট ছাড়াও চলে।

## ফোনে এক ধাপে ইনস্টলের ব্যবস্থা (একবারই সেটআপ, ~১০ মিনিট)

APK তৈরি করতে Android-এর বিল্ড টুল লাগে। GitHub বিনা খরচে এই কাজটি করে দেয়।

1. https://github.com এ একটি অ্যাকাউন্ট খুলুন (না থাকলে)।
2. **New repository** চাপুন।
   - নাম দিন, যেমন `tax-report`।
   - **Public** বেছে নিন। অ্যাপে কোনো করদাতার তথ্য নেই।
   - Create চাপুন।
3. **uploading an existing file** লিংকে চাপুন।
   - এই zip খুলে `TaxReportApp` ফোল্ডারের ভেতরের সবকিছু টেনে এনে ছাড়ুন। `.github` ও `app` ফোল্ডারসহ সব যেতে হবে।
   - তারপর **Commit changes** চাপুন।
   - Windows-এ `.github` ফোল্ডারটি লুকানো থাকতে পারে। File Explorer-এ View › Hidden items চালু করুন।
4. **Actions** ট্যাবে "Build APK" চলবে, প্রায় ৫ মিনিট লাগবে। সবুজ টিক এলে কাজ শেষ।
5. এখন থেকে ফোনে শুধু এই লিংক খুলুন (নিজের নাম বসিয়ে):

   `https://github.com/<আপনার-ইউজারনেম>/tax-report/releases/latest/download/TaxReport.apk`

   APK নামবে, খুললেই ইনস্টল হবে। প্রথমবার ফোন "অজানা উৎস থেকে ইনস্টল" এর অনুমতি চাইবে। অনুমতি দিন।

লিংকটি সহকর্মীদেরও দেওয়া যায়।

## নতুন সংস্করণ
নতুন `index.html` পেলে GitHub-এ `app/src/main/assets/index.html` ফাইলটি প্রতিস্থাপন করুন।
- ফাইল খুলে ✏️ বা "Upload files" দিয়ে করা যায়।
- `app/build.gradle` এ `versionCode` এক বাড়ান।

কয়েক মিনিটে একই লিংকে নতুন APK চলে আসবে। পুরোনো অ্যাপের উপর ইনস্টল করলে এন্ট্রি দেওয়া তথ্য থেকে যাবে, কারণ স্বাক্ষর-কী (`app/release.jks`) একই থাকে।

## Android Studio দিয়ে (বিকল্প)
1. File › Open দিয়ে `TaxReportApp` ফোল্ডার খুলুন।
2. Build › Generate Signed App Bundle / APK অথবা Build › Build APK(s) চাপুন।

## অ্যাপে ফাইল সংরক্ষণ ও PDF
- **Download Word / Save data:** Android-এর "Save as" পর্দা খোলে। ফাইলের নাম হয় "কর ফাঁকির তদন্ত প্রতিবেদন-করদাতার নাম"।
- **Download PDF:** প্রিন্ট পর্দায় প্রিন্টার হিসেবে "Save as PDF" বেছে নিন।
- **Import files:** ফোনের ফাইল থেকে .xlsx, .docx বা সংরক্ষিত .json বেছে নিন।

## প্রযুক্তিগত তথ্য
- Java-তে লেখা একটি Activity ও একটি WebView। কোনো বাইরের লাইব্রেরি নেই।
- minSdk 24 (Android 7.0), targetSdk 34, Android Gradle Plugin 8.5.2, Gradle 8.7, JDK 17।

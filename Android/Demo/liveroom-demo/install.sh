if ! [ $1 ]; then
  ./gradlew clean assembleDebug -PwithGradlePropertyAppId=true
fi

apk=app/build/outputs/apk/debug/app-debug.apk

echo "adb install -d -r $apk"
adb install -d -r $apk
sleep 1s
adb shell am start com.aliyun.liveroom.demo/.MainActivity

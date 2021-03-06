if ! [ $1 ]; then
  echo 'start build..'
  ./gradlew app:clean
  ./gradlew app:assembleDebug -PwithGradlePropertyAppId=true --stacktrace --info;
  
fi

echo "开始安装 $apk"
adb devices > devices.txt
echo "开始读取设备"

line_num=0
apk=app/build/outputs/apk/debug/app-debug.apk
while read -r line
do
	if [ $line_num != 0 ] && [ -n "$line" ];
	then
		devices_info=`echo $line | cut -d " " -f 1`
		echo $devices_info
		echo "adb -s $devices_info install -d -r $apk"
		adb -s $devices_info install -d -r $apk
	fi
	let line_num++
done < devices.txt

rm -f devices.txt

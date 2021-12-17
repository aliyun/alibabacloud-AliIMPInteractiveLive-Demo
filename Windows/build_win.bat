:: check build mode
@set BuildParam=%1
@if not "%BuildParam%"=="Release" set BuildParam="Debug"
@echo "build mode: " %BuildParam%

:: set debug or release
@set debug_type=1
@if "%BuildParam%"=="Release" set debug_type=0

@set build_dir=build
@if "%BuildParam%"=="Release" set build_dir=build_rel

:: specify the obs_studio src root.
set demo_root=%~dp0%

:: specify the Qt installation path
set QTDIR=D:\Qt\Qt5.12.2\5.12.2\msvc2017

:: set vs2017
set vs2017="C:\Program Files (x86)\Microsoft Visual Studio\2017\Professional\Common7\IDE\devenv.exe"

@mkdir %build_dir%
@cd/d %demo_root%/%build_dir%

@set build_type="RelWithDebInfo|Win32"
@if {%debug_type%} == {1} (set build_type="Debug|Win32")

@set config_mode="RelWithDebInfo"
@if {%debug_type%} == {1} (set config_mode="Debug")

call cmake -G "Visual Studio 15 2017" -DCMAKE_BUILD_TYPE=%config_mode% ..

:: build with vs2017
@del build_demo.txt

call %vs2017% %demo_root%/%build_dir%/demo.sln /build %build_type% /out build_demo.txt

@if "%BuildParam%"=="Release" (
xcopy /S /I /F /Y "%demo_root%/locale" "%demo_root%/build_rel/rundir/RelWithDebInfo/bin/32bit/data/locale"
) else (
xcopy /S /I /F /Y "%demo_root%/locale" "%demo_root%/build/rundir/Debug/bin/32bit/data/locale"
)

::call %vs2017% %demo_root%/%build_dir%/demo.sln

@cd/d %demo_root%

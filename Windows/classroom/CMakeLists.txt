project(demo)

add_definitions(-DNOMINMAX)
add_definitions(-DCOMPONENT_BUILD)
add_definitions(-DBASE_IMPLEMENTATION)
add_definitions(-DWIN32_LEAN_AND_MEAN)

include_directories(
"${CMAKE_SOURCE_DIR}/deps/meta/include"
"${CMAKE_SOURCE_DIR}/deps/meta/include/rtc"
"${CMAKE_SOURCE_DIR}/deps/meta/include/room"
"${CMAKE_SOURCE_DIR}/deps/meta/include/wb"
"${CMAKE_SOURCE_DIR}/deps/meta/include/meta"
"${CMAKE_SOURCE_DIR}/deps/dps/public/include"
"${CMAKE_SOURCE_DIR}/deps/oss_sdk/include"
"${CMAKE_SOURCE_DIR}/deps/boost/include/boost-1_77"
)

include_directories("${CMAKE_SOURCE_DIR}/deps/AliRTCSdk/include")

set(main_SOURCES
	main.cpp
	prm_map.h
	prm_map.cpp
)

if(DEFINED QTDIR${_lib_suffix})
	list(APPEND CMAKE_PREFIX_PATH "${QTDIR${_lib_suffix}}")
elseif(DEFINED QTDIR)
	list(APPEND CMAKE_PREFIX_PATH "${QTDIR}")
elseif(DEFINED ENV{QTDIR${_lib_suffix}})
	list(APPEND CMAKE_PREFIX_PATH "$ENV{QTDIR${_lib_suffix}}")
elseif(DEFINED ENV{QTDIR})
	list(APPEND CMAKE_PREFIX_PATH "$ENV{QTDIR}")
endif()

set(CMAKE_INCLUDE_CURRENT_DIR TRUE)
set(CMAKE_AUTOMOC TRUE)
if(WIN32)
	set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} /MP /wd4100 /wd4310 /wd4512 /EHa")
endif()
if(CMAKE_SIZEOF_VOID_P EQUAL 8)
	set(_lib_suffix 64)
else()
	set(_lib_suffix 32)
endif()

find_package(Qt5Widgets ${FIND_MODE})
find_package(Qt5Network ${FIND_MODE})
if(WIN32)
    find_package(Qt5WinExtras REQUIRED)
    include_directories(${Qt5WinExtras_INCLUDE_DIRS})
    add_definitions(-DQT_WINEXTRAS)
    set(QT5_WINEXTRAS_LIBS ${Qt5WinExtras_LIBRARIES})
endif()

if(APPLE)
	find_package(Qt5MacExtras REQUIRED)
endif(APPLE)


if(NOT Qt5Widgets_FOUND)
	if (ENABLE_UI)
		message(FATAL_ERROR "Failed to find Qt5")
	else()
		message(STATUS "Qt5 not found - UI disabled")
		return()
	endif()
endif()


if(WIN32)
	
	if(CMAKE_BUILD_TYPE STREQUAL Debug)
		link_directories("${CMAKE_SOURCE_DIR}/deps/meta/lib/win/Debug")
		link_directories("${CMAKE_SOURCE_DIR}/deps/meta/lib/win/Debug")
		link_directories("${CMAKE_SOURCE_DIR}/deps/oss_sdk/lib/win/Debug")
		link_directories("${CMAKE_SOURCE_DIR}/deps/OpenSSL/lib/win")
		link_directories("${CMAKE_SOURCE_DIR}/deps/curl/lib")
		link_directories("${CMAKE_SOURCE_DIR}/deps/boost/lib")
		set(MAINEXE_LIBRARIES 
			"meta.dll.lib"
			"libeay32.lib"
			"ssleay32.lib"
			"libcurl.lib"
			"atls.lib"
			"alibabacloud-oss-cpp-sdk.lib")
	else()
		link_directories("${CMAKE_SOURCE_DIR}/deps/meta/lib/win/Release")
		link_directories("${CMAKE_SOURCE_DIR}/deps/meta/lib/win/Release")
		link_directories("${CMAKE_SOURCE_DIR}/deps/oss_sdk/lib/win/Release")
		link_directories("${CMAKE_SOURCE_DIR}/deps/OpenSSL/lib/win")
		link_directories("${CMAKE_SOURCE_DIR}/deps/curl/lib")
		link_directories("${CMAKE_SOURCE_DIR}/deps/boost/lib")
		set(MAINEXE_LIBRARIES 
			"meta.dll.lib"
			"libeay32.lib"
			"ssleay32.lib"
			"atls.lib"
			"libcurl.lib"
			"alibabacloud-oss-cpp-sdk.lib")
	endif()
elseif(APPLE)
	set(EXTRA_LIBS ${CORE_FOUNDATION} ${APPLICATION_SERVICES} ${CORE_SERVICES})
	link_directories("${CMAKE_SOURCE_DIR}/deps/meta/lib/mac")
	if(CMAKE_BUILD_TYPE STREQUAL Debug)
		set(MAINEXE_LIBRARIES 
			"-framework Foundation"
			"-framework CoreFoundation"
			"-framework AppKit")
	else()
		set(MAINEXE_LIBRARIES 
			"-framework Foundation"
			"-framework CoreFoundation"
			"-framework AppKit")
	endif()
	
endif()


if(WIN32)
	set(classroom_PLATFORM_SOURCES
	)
	
elseif(APPLE)
	set(classroom_PLATFORM_SOURCES
	)

	find_library(IOKIT IOKit)
	include_directories(${IOKIT})
	find_package(AppKit REQUIRED)
	set(classroom_PLATFORM_LIBRARIES 
		${APPKIT_LIBRARIES}
		${IOKIT}
		"-framework ExceptionHandling"
		Qt5::MacExtras)

	add_definitions(-fobjc-arc)
endif()

set( DTGuiToolkit_SOURCES
	.
	./ui/Button/multi_icon_button.cpp
	./ui/Button/multi_icon_button.h
	./ui/Label/MultiIconLabel.h
	./ui/Label/MultiIconLabel.cpp
)
source_group(DTGuiToolkit FILES ${DTGuiToolkit_SOURCES})

set(classroom_SOURCES
	${classroom_PLATFORM_SOURCES})

set(classroom_HEADERS
	)

set(classroom_UI
	./forms/login_window.ui
	./forms/title_widget.ui
	./forms/class_main_window.ui
	./forms/chat_widget.ui
	./forms/user_list_widget.ui
	./forms/user_item_widget.ui
	./forms/toast_widget.ui
	./forms/small_preview_controls.ui
	./forms/confirm_dialog.ui
	./forms/main_display_preview.ui
	./forms/main_display_container.ui
	./forms/grid_display_widget.ui
	./forms/speaker_display_widget.ui
	./forms/grid_display_item_widget.ui
	./forms/bottom_widget.ui
	./forms/conf_share_dialog.ui
	./forms/conf_performance_dialog.ui
	./forms/display_controls.ui
	./forms/student_controls.ui
	./forms/share_screen_item_widget.ui
	./forms/share_screen_select_dlg.ui
	./forms/thumbnail_container.ui
	./forms/thumbnail_widget.ui
	./forms/thumbnail_widget_item.ui
	./forms/wb_tips.ui
	./forms/size_slide_bar.ui
	./forms/pen_selector_menu.ui
	./forms/text_selector_menu.ui
	./forms/upload_selector_menu.ui
	./forms/laser_selector_menu.ui
	./forms/share_screen_hover_bar.ui
	./forms/recording_tips.ui
	./forms/switch_zoom.ui
	./forms/ppt_upload_progress.ui
	./ui/Button/multi_icon_button.ui
	)

set(classroom_QRC
	./forms/demo.qrc)


set(common_SOURCES
	./common/common_helper.cpp
	./common/http_helper.cpp
	./common/icon_font_helper.cpp
	./common/logging.cpp
	./common/common_helper.h
	./common/http_helper.h
	./common/icon_font_helper.h
	./common/iconfont_define.h
	./common/logging.h
	)

source_group(common FILES ${common_SOURCES})

set(util_SOURCES
  ./util/sign_util.cpp
  ./util/sign_util.h
  ./util/log_util.cpp
  ./util/log_util.h)
source_group(util FILES ${util_SOURCES})

set(api_SOURCES
  ./api/base_api.cpp
  ./api/base_api.h)

source_group(api FILES ${api_SOURCES})

set(const_SOURCES
  ./const/const.h
  ./const/const.cpp)
source_group(const FILES ${const_SOURCES})

set(event_SOURCES
  ./event/event_manager.cpp
  ./event/event_manager.h
  ./event/event_param.h)
  
set(biz_SOURCES
  ./biz/rtc_user_data_mgr.cpp
  ./biz/rtc_user_data_mgr.h) 
source_group(event FILES ${event_SOURCES})

set( ui_SOURCES
	./view/interface/i_login_window.h
	./view/interface/i_main_window.h
	./view/view_defines.h
	./view/view_component_manager.h
	./view/view_component_manager.cpp
	login_window.h
	login_window.cpp
	login_window_vm.h
	login_window_vm.cpp
	user_list_widget.h
	user_list_widget.cpp
	user_list_widget_vm.h
	user_list_widget_vm.cpp
	user_item_widget.h
	user_item_widget.cpp
	user_item_widget_vm.h
	user_item_widget_vm.cpp
	bottom_widget.h
	bottom_widget.cpp
	title_widget.h
	title_widget.cpp
	title_widget_vm.h
	title_widget_vm.cpp
	class_main_window.h
	class_main_window.cpp
	class_main_window_vm.h
	class_main_window_vm.cpp
	chat_vm.h
	chat_vm.cpp
	chat_listener.h
	chat_listener.cpp
	chat_widget.h
	chat_widget.cpp
	chat_bubble.h
	chat_bubble.cpp
	emoji_table.h
	emoji_table.cpp
	bottom_widget_teacher_vm.h
	bottom_widget_vm.cpp
	bottom_widget_vm.h
	bottom_widget_student_vm.cpp
	bottom_widget_student_vm.h
	bottom_widget_teacher_vm.cpp
	device_list.cpp
	device_list.h
	device_list_vm.cpp
	device_list_vm.h
	toast_widget.h
	toast_widget.cpp
	small_preview_controls.h
	small_preview_controls.cpp
	confirm_dialog.h
	confirm_dialog.cpp
	main_display_preview_vm.h
	main_display_preview_vm.cpp
	main_display_preview.h
	main_display_preview.cpp
	scheme_login.h
	scheme_login.cc
	main_display_container.cpp
	main_display_container.h
	grid_display_widget.cpp
	grid_display_widget.h
	speaker_display_widget.cpp
	speaker_display_widget.h
	grid_display_item_widget.cpp
	grid_display_item_widget.h
	video_display_mgr.h
	video_display_mgr.cpp
	conf_performance_dialog.cpp
	conf_performance_dialog.h
	conf_share_dialog.cpp
	conf_share_dialog.h
	qwidget_event_filter.h
	qwidget_event_filter.cpp
	classroom_def.h
	classroom_def.cpp
	framelesswindow.h
	framelesswindow.cpp
	display_controls.h
	display_controls.cpp
	student_controls.h
	student_controls.cpp
	classroom_application.h
	share_screen_select_dlg.h
	share_screen_select_dlg.cpp
	share_screen_item_widget.h
	share_screen_item_widget.cpp
	thumbnail_container.h
	thumbnail_container.cpp
	thumbnail_widget.h
	thumbnail_widget.cpp
	thumbnail_widget_item.h
	thumbnail_widget_item.cpp
	white_board_tips.cpp
	white_board_tips.h
	size_slide_bar.h
	pen_selector_menu.h
	text_selector_menu.h
	upload_selector_menu.h
	laser_selector_menu.h
	size_slide_bar.cpp
	pen_selector_menu.cpp
	text_selector_menu.cpp
	upload_selector_menu.cpp
	laser_selector_menu.cpp
	thumbnail_loader.h
	thumbnail_loader.cpp
	thumbnail_loader_http_requester.h
	thumbnail_loader_http_requester.cpp
	share_screen_hover_bar_dlg.h
	share_screen_hover_bar_dlg.cpp
	recording_tips_dialog.h
	recording_tips_dialog.cpp
	switch_zoom_dialog.h
	switch_zoom_dialog.cpp
	ppt_upload_progress.h
	ppt_upload_progress.cpp
)
source_group(ui FILES ${ui_SOURCES})  

qt5_wrap_ui(classroom_UI_HEADERS ${classroom_UI})
qt5_add_resources(classroom_QRC_SOURCES ${classroom_QRC})


add_executable(demo WIN32
	${main_SOURCES}
	${platform_SOURCES}
	${common_SOURCES}
	${util_SOURCES}
	${api_SOURCES}
	${const_SOURCES}
	${event_SOURCES}
	${classroom_SOURCES}
	${classroom_UI_HEADERS}
	${ui_SOURCES}
	${classroom_QRC_SOURCES}
	${biz_SOURCES}
	${DTGuiToolkit_SOURCES}
	)

target_link_libraries(demo
	${MAINEXE_LIBRARIES}
	Qt5::Widgets
	Qt5::Network
	${QT5_WINEXTRAS_LIBS}
	)

set(DEMO_LIBRARY_DESTINATION
	"bin/32bit")
set(DEMO_EXECUTABLE_DESTINATION
	"bin/32bit")
install(TARGETS demo
		LIBRARY DESTINATION "${DEMO_LIBRARY_DESTINATION}"
		RUNTIME DESTINATION "${DEMO_EXECUTABLE_DESTINATION}")

add_custom_command(
		TARGET demo POST_BUILD
		COMMAND "${CMAKE_COMMAND}" -E copy
			"$<TARGET_FILE:demo>"
			"${CMAKE_BINARY_DIR}/rundir/$<CONFIGURATION>/bin/32bit/$<TARGET_FILE_NAME:demo>"
		VERBATIM)
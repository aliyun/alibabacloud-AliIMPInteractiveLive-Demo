# Doesn't realy make sense anywhere else
if(NOT MSVC)
	return()
endif()

# Internal variable to avoid copying more than once
if(COPIED_DEPENDENCIES)
	return()
endif()

option(COPY_DEPENDENCIES "Automaticaly try copying all dependencies" ON)
if(NOT COPY_DEPENDENCIES)
	return()
endif()

if (CMAKE_CONFIGURATION_TYPES MATCHES "Debug")
	file(GLOB QT_DEBUG_BIN_FILES
		"${Qt5Core_DIR}/../../../bin/Qt5Cored.dll"
		"${Qt5Core_DIR}/../../../bin/Qt5Guid.dll"
		"${Qt5Core_DIR}/../../../bin/Qt5Widgetsd.dll"
		"${Qt5Core_DIR}/../../../bin/Qt5WinExtrasd.dll"
		"${Qt5Core_DIR}/../../../bin/Qt5Positioningd.dll"
		"${Qt5Core_DIR}/../../../bin/Qt5Networkd.dll"
		"${Qt5Core_DIR}/../../../bin/libGLESv2d.dll"
		"${Qt5Core_DIR}/../../../bin/libEGLd.dll"
		"${CMAKE_SOURCE_DIR}/deps/dps/libs/win/Debug/dps.dll"
		"${CMAKE_SOURCE_DIR}/deps/fml/libs/win/Debug/fml.dll"
		"${CMAKE_SOURCE_DIR}/deps/gaea/libs/win/Debug/libeay32MDd.dll"
		"${CMAKE_SOURCE_DIR}/deps/gaea/libs/win/Debug/ssleay32MDd.dll"
		"${CMAKE_SOURCE_DIR}/deps/OpenSSL/lib/win/*.dll"
		"${CMAKE_SOURCE_DIR}/deps/meta/lib/win/Debug/*.dll"
		"${CMAKE_SOURCE_DIR}/deps/AliRTCSdk/lib/win/Debug/*.dll"
		"${CMAKE_SOURCE_DIR}/deps/player/win/lib/Debug/*.dll"
		"${CMAKE_SOURCE_DIR}/deps/oss_sdk/lib/win/Debug/*.dll"
		"${CMAKE_SOURCE_DIR}/deps/curl/lib/*.dll"
		)
	file(GLOB QT_DEBUG_PLAT_BIN_FILES
		"${Qt5Core_DIR}/../../../plugins/platforms/qwindowsd.dll")
	file(GLOB QT_IMAGEFORMATS_DEBUG_BIN_FILES
		"${CMAKE_SOURCE_DIR}/deps/imageformats/debug/*.dll")
	file(GLOB WHITE_BOARD_LOCALES_DEBUG_BIN_FILES
		"${CMAKE_SOURCE_DIR}/deps/whiteboard/win/bin/Debug/locales/*.*")
	file(GLOB WHITE_BOARD_SWIFTSHADER_DEBUG_BIN_FILES
		"${CMAKE_SOURCE_DIR}/deps/whiteboard/win/bin/Debug/swiftshader/*.*")
	file(GLOB WHITE_BOARD_DEBUG_BIN_FILES
		"${CMAKE_SOURCE_DIR}/deps/whiteboard/win/bin/Debug/*.*")
	file(GLOB PLAYER_DEP_DEBUG_BIN_FILES 
		"${CMAKE_SOURCE_DIR}/deps/player/win/lib/Debug/AliPlayer.Dependencies/*.*")
endif()

if (CMAKE_CONFIGURATION_TYPES MATCHES "Rel")
	file(GLOB QT_BIN_FILES
		"${Qt5Core_DIR}/../../../bin/Qt5Core.dll"
		"${Qt5Core_DIR}/../../../bin/Qt5Gui.dll"
		"${Qt5Core_DIR}/../../../bin/Qt5Widgets.dll"
		"${Qt5Core_DIR}/../../../bin/Qt5WinExtras.dll"
		"${Qt5Core_DIR}/../../../bin/Qt5Positioning.dll"
		"${Qt5Core_DIR}/../../../bin/Qt5Network.dll"
		"${Qt5Core_DIR}/../../../bin/libGLESv2.dll"
		"${Qt5Core_DIR}/../../../bin/libEGL.dll"
		"${CMAKE_SOURCE_DIR}/deps/dps/libs/win/Release/dps.dll"
		"${CMAKE_SOURCE_DIR}/deps/fml/libs/win/Release/fml.dll"
		"${CMAKE_SOURCE_DIR}/deps/gaea/libs/win/Release/libeay32MD.dll"
		"${CMAKE_SOURCE_DIR}/deps/gaea/libs/win/Release/ssleay32MD.dll"
		"${CMAKE_SOURCE_DIR}/deps/OpenSSL/lib/win/*.dll"
		"${CMAKE_SOURCE_DIR}/deps/meta/lib/win/Release/*.dll"
		"${CMAKE_SOURCE_DIR}/deps/AliRTCSdk/lib/win/Release/*.dll"
		"${CMAKE_SOURCE_DIR}/deps/player/win/lib/Release/*.dll"
		"${CMAKE_SOURCE_DIR}/deps/oss_sdk/lib/win/Release/*.dll"
		"${CMAKE_SOURCE_DIR}/deps/curl/lib/*.dll"
		)
	file(GLOB QT_PLAT_BIN_FILES
		"${Qt5Core_DIR}/../../../plugins/platforms/qwindows.dll")
	file(GLOB WHITE_BOARD_LOCALES_REL_BIN_FILES
		"${CMAKE_SOURCE_DIR}/deps/whiteboard/win/bin/Release/locales/*.*")
	file(GLOB WHITE_BOARD_SWIFTSHADER_REL_BIN_FILES
		"${CMAKE_SOURCE_DIR}/deps/whiteboard/win/bin/Release/swiftshader/*.*")
	file(GLOB WHITE_BOARD_REL_BIN_FILES
		"${CMAKE_SOURCE_DIR}/deps/whiteboard/win/bin/Release/*.*")
	file(GLOB PLAYER_DEP_REL_BIN_FILES
		"${CMAKE_SOURCE_DIR}/deps/player/win/lib/Release/AliPlayer.Dependencies/*.*")
endif()

set(ALL_WHITE_BOARD_LOCALES_REL_BIN_FILES
	${WHITE_BOARD_LOCALES_REL_BIN_FILES})

set(WHITE_BOARD_SWIFTSHADER_REL_BIN_FILES
	${WHITE_BOARD_SWIFTSHADER_REL_BIN_FILES})

set(WHITE_BOARD_REL_BIN_FILES
	${WHITE_BOARD_REL_BIN_FILES})

set(ALL_WHITE_BOARD_LOCALES_DEBUG_BIN_FILES
	${WHITE_BOARD_LOCALES_DEBUG_BIN_FILES})

set(WHITE_BOARD_SWIFTSHADER_DEBUG_BIN_FILES
	${WHITE_BOARD_SWIFTSHADER_DEBUG_BIN_FILES})

set(WHITE_BOARD_DEBUG_BIN_FILES
	${WHITE_BOARD_DEBUG_BIN_FILES})

set(ALL_REL_BIN_FILES
	${QT_BIN_FILES})

set(ALL_DBG_BIN_FILES
	${QT_DEBUG_BIN_FILES})

set(ALL_PLATFORM_REL_BIN_FILES
	${QT_PLAT_BIN_FILES})

set(ALL_PLATFORM_DBG_BIN_FILES
	${QT_DEBUG_PLAT_BIN_FILES})
	
set(ALL_IMAGEFORMATS_DEBUG_BIN_FILES
	${QT_IMAGEFORMATS_DEBUG_BIN_FILES})
	
set(ALL_IMAGEFORMATS_REL_BIN_FILES
	${QT_IMAGEFORMATS_BIN_FILES})
	
set(PLAYER_DEP_DEBUG_BIN_FILES
	${PLAYER_DEP_DEBUG_BIN_FILES})

set(PLAYER_DEP_REL_BIN_FILES
	${PLAYER_DEP_REL_BIN_FILES})
foreach(list
		ALL_REL_BIN_FILES ALL_DBG_BIN_FILES
		ALL_PLATFORM_REL_BIN_FILES ALL_PLATFORM_DBG_BIN_FILES)
	if(${list})
		list(REMOVE_DUPLICATES ${list})
	endif()
endforeach()

foreach(BinFile ${ALL_DBG_BIN_FILES})
	message(STATUS "copying ${BinFile} to ${CMAKE_SOURCE_DIR}/build/rundir/Debug/bin/32bit")
	file(COPY "${BinFile}" DESTINATION "${CMAKE_SOURCE_DIR}/build/rundir/Debug/bin/32bit/")
endforeach()

foreach(BinFile ${ALL_REL_BIN_FILES})
	message(STATUS "copying ${BinFile} to ${CMAKE_SOURCE_DIR}/build_rel/rundir/RelWithDebInfo/bin/32bit")
	file(COPY "${BinFile}" DESTINATION "${CMAKE_SOURCE_DIR}/build_rel/rundir/RelWithDebInfo/bin/32bit/")
endforeach()

foreach(BinFile ${ALL_PLATFORM_REL_BIN_FILES})
	make_directory("${CMAKE_SOURCE_DIR}/build/rundir/Debug/bin/32bit/platforms")
	file(COPY "${BinFile}" DESTINATION "${CMAKE_SOURCE_DIR}/build/rundir/Debug/bin/32bit/platforms/")
endforeach()

foreach(BinFile ${ALL_PLATFORM_DBG_BIN_FILES})
	make_directory("${CMAKE_SOURCE_DIR}/build_rel/rundir/RelWithDebInfo/bin/32bit/platforms")
	file(COPY "${BinFile}" DESTINATION "${CMAKE_SOURCE_DIR}/build_rel/rundir/RelWithDebInfo/bin/32bit/platforms/")
endforeach()

foreach(BinFile ${ALL_IMAGEFORMATS_DEBUG_BIN_FILES})
	make_directory("${CMAKE_SOURCE_DIR}/build/rundir/Debug/bin/32bit/imageformats")
	file(COPY "${BinFile}" DESTINATION "${CMAKE_SOURCE_DIR}/build/rundir/Debug/bin/32bit/imageformats/")
endforeach()

foreach(BinFile ${ALL_IMAGEFORMATS_REL_BIN_FILES})
	make_directory("${CMAKE_SOURCE_DIR}/build_rel/rundir/RelWithDebInfo/bin/32bit/imageformats")
	file(COPY "${BinFile}" DESTINATION "${CMAKE_SOURCE_DIR}/build_rel/rundir/RelWithDebInfo/bin/32bit/imageformats/")
endforeach()

foreach(BinFile ${ALL_WHITE_BOARD_LOCALES_REL_BIN_FILES})
	make_directory("${CMAKE_SOURCE_DIR}/build_rel/rundir/RelWithDebInfo/bin/32bit/locales")
	file(COPY "${BinFile}" DESTINATION "${CMAKE_SOURCE_DIR}/build_rel/rundir/RelWithDebInfo/bin/32bit/locales/")
endforeach()

foreach(BinFile ${WHITE_BOARD_SWIFTSHADER_REL_BIN_FILES})
	make_directory("${CMAKE_SOURCE_DIR}/build_rel/rundir/RelWithDebInfo/bin/32bit/swiftshader")
	file(COPY "${BinFile}" DESTINATION "${CMAKE_SOURCE_DIR}/build_rel/rundir/RelWithDebInfo/bin/32bit/swiftshader/")
endforeach()

foreach(BinFile ${WHITE_BOARD_REL_BIN_FILES})
	make_directory("${CMAKE_SOURCE_DIR}/build_rel/rundir/RelWithDebInfo/bin/32bit")
	file(COPY "${BinFile}" DESTINATION "${CMAKE_SOURCE_DIR}/build_rel/rundir/RelWithDebInfo/bin/32bit/")
endforeach()


foreach(BinFile ${ALL_WHITE_BOARD_LOCALES_DEBUG_BIN_FILES})
	make_directory("${CMAKE_SOURCE_DIR}/build/rundir/Debug/bin/32bit/locales")
	file(COPY "${BinFile}" DESTINATION "${CMAKE_SOURCE_DIR}/build/rundir/Debug/bin/32bit/locales/")
endforeach()

foreach(BinFile ${WHITE_BOARD_SWIFTSHADER_DEBUG_BIN_FILES})
	make_directory("${CMAKE_SOURCE_DIR}/build/rundir/Debug/bin/32bit/swiftshader")
	file(COPY "${BinFile}" DESTINATION "${CMAKE_SOURCE_DIR}/build/rundir/Debug/bin/32bit/swiftshader/")
endforeach()

foreach(BinFile ${WHITE_BOARD_DEBUG_BIN_FILES})
	make_directory("${CMAKE_SOURCE_DIR}/build/rundir/Debug/bin/32bit")
	file(COPY "${BinFile}" DESTINATION "${CMAKE_SOURCE_DIR}/build/rundir/Debug/bin/32bit/")
endforeach()

foreach(BinFile ${PLAYER_DEP_DEBUG_BIN_FILES})
	make_directory("${CMAKE_SOURCE_DIR}/build/rundir/Debug/bin/32bit/AliPlayer.Dependencies")
	file(COPY "${BinFile}" DESTINATION "${CMAKE_SOURCE_DIR}/build/rundir/Debug/bin/32bit/AliPlayer.Dependencies")
endforeach()

foreach(BinFile ${PLAYER_DEP_REL_BIN_FILES})
	make_directory("${CMAKE_SOURCE_DIR}/build_rel/rundir/RelWithDebInfo/bin/32bit/AliPlayer.Dependencies")
	file(COPY "${BinFile}" DESTINATION "${CMAKE_SOURCE_DIR}/build_rel/rundir/RelWithDebInfo/bin/32bit/AliPlayer.Dependencies/")
endforeach()
set(COPIED_DEPENDENCIES TRUE CACHE BOOL "Dependencies have been copied, set to false to copy again" FORCE)

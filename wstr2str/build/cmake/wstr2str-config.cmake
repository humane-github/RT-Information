# wstr2str CMake config file
#
# This file sets the following variables:
# wstr2str_FOUND - Always TRUE.
# wstr2str_INCLUDE_DIRS - Directories containing the wstr2str include files.
# wstr2str_IDL_DIRS - Directories containing the wstr2str IDL files.
# wstr2str_LIBRARIES - Libraries needed to use wstr2str.
# wstr2str_DEFINITIONS - Compiler flags for wstr2str.
# wstr2str_VERSION - The version of wstr2str found.
# wstr2str_VERSION_MAJOR - The major version of wstr2str found.
# wstr2str_VERSION_MINOR - The minor version of wstr2str found.
# wstr2str_VERSION_REVISION - The revision version of wstr2str found.
# wstr2str_VERSION_CANDIDATE - The candidate version of wstr2str found.

message(STATUS "Found wstr2str-1.0.0")
set(wstr2str_FOUND TRUE)

find_package(<dependency> REQUIRED)

#set(wstr2str_INCLUDE_DIRS
#    "C:/Program Files/wstr2str/include/wstr2str-1"
#    ${<dependency>_INCLUDE_DIRS}
#    )
#
#set(wstr2str_IDL_DIRS
#    "C:/Program Files/wstr2str/include/wstr2str-1/idl")
set(wstr2str_INCLUDE_DIRS
    "C:/Program Files/wstr2str/include/"
    ${<dependency>_INCLUDE_DIRS}
    )
set(wstr2str_IDL_DIRS
    "C:/Program Files/wstr2str/include//idl")


if(WIN32)
    set(wstr2str_LIBRARIES
        "C:/Program Files/wstr2str/components/lib/wstr2str.lib"
        ${<dependency>_LIBRARIES}
        )
else(WIN32)
    set(wstr2str_LIBRARIES
        "C:/Program Files/wstr2str/components/lib/wstr2str.dll"
        ${<dependency>_LIBRARIES}
        )
endif(WIN32)

set(wstr2str_DEFINITIONS ${<dependency>_DEFINITIONS})

set(wstr2str_VERSION 1.0.0)
set(wstr2str_VERSION_MAJOR 1)
set(wstr2str_VERSION_MINOR 0)
set(wstr2str_VERSION_REVISION 0)
set(wstr2str_VERSION_CANDIDATE )


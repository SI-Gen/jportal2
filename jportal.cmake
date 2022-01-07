
find_package(Java 1.8 COMPONENTS Runtime REQUIRED)
get_filename_component(jportaljar "${CONAN_JPORTAL_ROOT}/jportal2-1.6.0-SNAPSHOT.jar" ABSOLUTE)

macro(Jportal)
SET(args "")

foreach(loop_var ${ARGN})
    LIST(APPEND args "${loop_var}")
endforeach()

message(STATUS "Executing JPortal Command ${Java_JAVA_EXECUTABLE} -jar ${jportaljar} ${args}")
execute_process(COMMAND ${Java_JAVA_EXECUTABLE} -jar ${jportaljar}
    ${args}
)
endmacro(Jportal)

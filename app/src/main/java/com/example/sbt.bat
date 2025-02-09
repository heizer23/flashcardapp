@echo off
setlocal enabledelayedexpansion

REM Set the root directory to start searching from
set "ROOT_DIR=%~dp0"

REM Get the directory where the batch file is located
set "BATCH_DIR=%~dp0"

REM Start processing directories recursively
for /r "%ROOT_DIR%" %%d in (.) do (
    REM Extract the name of the current directory
    set "SUBDIR_NAME=%%~nxd"

    REM Set the destination XML file in the batch file's directory
    set "DEST_FILE=%BATCH_DIR%!SUBDIR_NAME!.xml"

    REM Clear the destination file if it already exists
    if exist "!DEST_FILE!" del "!DEST_FILE!"

    REM Start XML structure
    (
        echo ^<?xml version="1.0" encoding="UTF-8" ?^>
        echo ^<directory name="%%~nxd" path="%%~dpd"^>
    ) > "!DEST_FILE!"

    REM Flag to track if any Java files were found
    set "found_files=false"

    REM Find Java files in the current directory
    dir /b "%%d\*.java" > "%TEMP%\javafiles.tmp" 2>nul

    REM Process each Java file from the temporary list
    for /f "usebackq delims=" %%f in ("%TEMP%\javafiles.tmp") do (
        set "found_files=true"
        (
            echo   ^<file name="%%f" path="%%~dpd%%f"^>
            echo     ^<![CDATA[
        ) >> "!DEST_FILE!"
        type "%%d\%%f" >> "!DEST_FILE!"
        (
            echo     ]]^>
            echo   ^</file^>
        ) >> "!DEST_FILE!"
    )

    REM If no Java files were found, add an empty message
    if "!found_files!"=="false" (
        echo   ^<message^>No Java files found in this directory.^</message^> >> "!DEST_FILE!"
    )

    REM Clean up temporary file
    if exist "%TEMP%\javafiles.tmp" del "%TEMP%\javafiles.tmp"

    REM Close XML structure
    echo ^</directory^> >> "!DEST_FILE!"

    echo Processed directory "%%~nxd", output saved to "!DEST_FILE!"
)

echo Done creating XML files for each subdirectory.
pause

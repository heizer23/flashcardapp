@echo off
setlocal enabledelayedexpansion

REM Set the root directory to start searching from
set "ROOT_DIR=%~dp0"

REM Get the directory where the batch file is located
set "BATCH_DIR=%~dp0"

REM Loop through each subdirectory in the root directory
for /d %%d in ("%ROOT_DIR%*") do (
    REM Extract the name of the subdirectory
    set "SUBDIR_NAME=%%~nd"

    REM Set the destination file in the batch file's directory
    set "DEST_FILE=%BATCH_DIR%!SUBDIR_NAME!.txt"

    REM Clear the destination file if it already exists
    if exist "!DEST_FILE!" del "!DEST_FILE!"

    REM Flag to track if any Java files were found
    set "found_files=false"

    REM Traverse through the directories and append Java files
    for %%f in ("%%d\*.java") do (
        type "%%f" >> "!DEST_FILE!"
        echo. >> "!DEST_FILE!"
        set "found_files=true"
    )

    REM If no Java files were found, add a header text
    if "!found_files!"=="false" (
        echo No Java files found in directory %%d. >> "!DEST_FILE!"
    )

    echo Processed directory "%%d", output saved to "!DEST_FILE!"
)

echo Done creating text files for each subdirectory.
pause

import os
import re
import json
from pathlib import Path

# ------------------------------------------------------------------------
# 1) DETECT PROJECT ROOT & PATHS
# ------------------------------------------------------------------------
def get_project_root():
    """
    Determine the project root based on the script's location.
    Assumes the script is inside {ProjectRoot}/GptPipeline/.
    """
    script_path = Path(__file__).resolve()
    return script_path.parent.parent  # Moves up from /GptPipeline/ to project root

def get_project_name(project_root):
    """
    Extract the project name from the project root folder.
    """
    return project_root.name


# ------------------------------------------------------------------------
# 2) HELPER FOR JAVA/KOTLIN
# ------------------------------------------------------------------------
def get_package_name(code_str):
    """
    Extracts the package name from Java/Kotlin files.
    """
    match = re.search(r'^\s*package\s+([\w\.]+)', code_str, re.MULTILINE)
    return match.group(1) if match else "unknown"

def collect_java_kotlin_files(source_dir):
    """
    Collect all Java/Kotlin files from the source directory.
    """
    java_dict = {}

    for root, _, files in os.walk(source_dir):
        for filename in files:
            if filename.endswith((".kt", ".java")):
                file_path = Path(root) / filename

                with open(file_path, "r", encoding="utf-8") as f:
                    content = f.read()

                package_name = get_package_name(content)
                file_stem = os.path.splitext(filename)[0]

                if package_name not in java_dict:
                    java_dict[package_name] = {}

                java_dict[package_name][file_stem] = {
                    "file_path": str(file_path),
                    "content": content
                }

    return java_dict


# ------------------------------------------------------------------------
# 3) HELPER FOR LAYOUT XML
# ------------------------------------------------------------------------
def collect_layout_files(layout_dir):
    """
    Collect all layout XML files from the layout directory.
    """
    layout_dict = {}

    for root, _, files in os.walk(layout_dir):
        for filename in files:
            if filename.endswith(".xml"):
                file_path = Path(root) / filename

                with open(file_path, "r", encoding="utf-8") as f:
                    content = f.read()

                file_stem = os.path.splitext(filename)[0]

                layout_dict[file_stem] = {
                    "file_path": str(file_path),
                    "content": content
                }

    return layout_dict


# ------------------------------------------------------------------------
# 4) HELPER FOR VALUES XML
# ------------------------------------------------------------------------
def collect_values_files(values_dir):
    """
    Collect all values XML files except themes and colors.
    """
    exclude_files = {"colors.xml", "themes.xml", "theme_overlays.xml"}
    values_dict = {}

    for root, _, files in os.walk(values_dir):
        for filename in files:
            if filename.endswith(".xml") and filename not in exclude_files:
                file_path = Path(root) / filename

                with open(file_path, "r", encoding="utf-8") as f:
                    content = f.read()

                file_stem = os.path.splitext(filename)[0]

                values_dict[file_stem] = {
                    "file_path": str(file_path),
                    "content": content
                }

    return values_dict


def main():
    # Determine the project root and project name dynamically
    project_root = get_project_root()
    project_name = get_project_name(project_root)

    # Define standard Android paths based on detected project root
    java_source_dir = project_root / "app" / "src" / "main" / "java"
    layout_dir = project_root / "app" / "src" / "main" / "res" / "layout"
    values_dir = project_root / "app" / "src" / "main" / "res" / "values"

    # Define the output JSON path within the project's GptPipeline folder
    output_file = project_root / "GptPipeline" / "copied" / "classes.json"

    # --------------------------------------------------------------------
    # A) Collect Java/Kotlin
    # --------------------------------------------------------------------
    java_kotlin_dict = collect_java_kotlin_files(java_source_dir)

    # --------------------------------------------------------------------
    # B) Collect Layout XML
    # --------------------------------------------------------------------
    layout_files_dict = collect_layout_files(layout_dir)

    # --------------------------------------------------------------------
    # C) Collect Values XML (excluding themes and colors)
    # --------------------------------------------------------------------
    values_files_dict = collect_values_files(values_dir)

    # --------------------------------------------------------------------
    # Combine into final JSON
    # --------------------------------------------------------------------
    final_output = {
        "project_name": project_name,
        "java_kotlin_files": java_kotlin_dict,
        "layout_files": layout_files_dict,
        "values_files": values_files_dict
    }

    # Ensure output folder exists
    os.makedirs(output_file.parent, exist_ok=True)

    # Write JSON
    with open(output_file, "w", encoding="utf-8") as out_f:
        json.dump(final_output, out_f, indent=2)

    print(f"Extraction complete for project '{project_name}'.\nOutput: {output_file}")

if __name__ == "__main__":
    main()

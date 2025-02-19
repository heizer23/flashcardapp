import tkinter as tk
from tkinter import messagebox, scrolledtext
import os
import subprocess
import sys
import json
import pyperclip

# --------------------------------------------------------------------
# Constants and Paths
# --------------------------------------------------------------------
BASE_DIR = os.path.dirname(os.path.abspath(__file__))  # Folder containing this script
SOURCE_DIR = os.path.join(BASE_DIR, "copied")          # Folder where text and JSON files are stored
GET_FILES_SCRIPT = os.path.join(BASE_DIR, "getFiles.py")

# Special file names we always want to include:
PROMPT_FILE_NAME = "1 prompt.txt"
GENERAL_INSTRUCTIONS_FILE_NAME = "2 gemeral instructions .txt"
CLASSES_JSON_FILE_NAME = "classes.json"

PROMPT_FILE = os.path.join(SOURCE_DIR, PROMPT_FILE_NAME)
GENERAL_INSTRUCTIONS_FILE = os.path.join(SOURCE_DIR, GENERAL_INSTRUCTIONS_FILE_NAME)
CLASSES_JSON_FILE = os.path.join(SOURCE_DIR, CLASSES_JSON_FILE_NAME)
OUTPUT_FILE = os.path.join(SOURCE_DIR, "merged_output.txt")

# --------------------------------------------------------------------
# Utility Functions
# --------------------------------------------------------------------
def run_get_files_script():
    """
    Runs getFiles.py to regenerate classes.json (if it exists).
    """
    if os.path.exists(GET_FILES_SCRIPT):
        try:
            print(f"Running {GET_FILES_SCRIPT}...")
            result = subprocess.run(
                [sys.executable, GET_FILES_SCRIPT],
                capture_output=True,
                text=True,
                check=True
            )
            print("getFiles.py output:", result.stdout)
            print("getFiles.py errors:", result.stderr)
        except subprocess.CalledProcessError as e:
            messagebox.showerror(
                "Error",
                f"Failed to update {CLASSES_JSON_FILE_NAME}: {e}\n{e.stderr}"
            )

def read_text_file(filepath):
    """
    Safely reads a text file and returns its content.
    """
    try:
        with open(filepath, "r", encoding="utf-8") as f:
            return f.read()
    except Exception as e:
        messagebox.showerror("Error", f"Could not read {os.path.basename(filepath)}: {e}")
        return ""

def write_text_file(filepath, content):
    """
    Safely writes content to a text file.
    """
    try:
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(content)
    except Exception as e:
        messagebox.showerror("Error", f"Could not write to {os.path.basename(filepath)}: {e}")

# --------------------------------------------------------------------
# Merging Logic
# --------------------------------------------------------------------
def merge_and_copy_to_clipboard(
    second_text_content,
    file_vars,
    json_package_vars,
    json_class_vars,
    prompt_text_widget
):
    """
    Orchestrates the final merge:
      1) Saves changes to 1 prompt.txt from the top text area.
      2) Merges content in this order:
         - 1 prompt.txt
         - second text (from second text widget)
         - 2 gemeral instructions .txt
         - user-selected text files
         - user-selected JSON classes
      3) Copies everything to the clipboard, writes to merged_output.txt.

    :param second_text_content: Text from the second text box.
    :param file_vars: dict of {filename: BooleanVar} for normal text/json files.
    :param json_package_vars: dict of {package_name: BooleanVar}.
    :param json_class_vars: dict of {(package_name, class_name): BooleanVar}.
    :param prompt_text_widget: The ScrolledText widget containing 1 prompt.txt content.
    """
    # 1) Save changes to "1 prompt.txt"
    new_prompt_content = prompt_text_widget.get("1.0", tk.END).strip()
    write_text_file(PROMPT_FILE, new_prompt_content)

    # 2) Start building the merged content
    merged_content = ""

    # Always include 1 prompt.txt
    prompt_content = read_text_file(PROMPT_FILE)
    merged_content += f"--- {PROMPT_FILE_NAME} ---\n{prompt_content}\n\n"

    # Insert second text content
    if second_text_content.strip():
        merged_content += "--- Additional Text Field ---\n"
        merged_content += second_text_content.strip() + "\n\n"

    # Always include 2 gemeral instructions .txt
    general_instructions_content = read_text_file(GENERAL_INSTRUCTIONS_FILE)
    merged_content += f"--- {GENERAL_INSTRUCTIONS_FILE_NAME} ---\n{general_instructions_content}\n\n"

    # Merge user-selected text files (excluding the special ones)
    for file_name, var in file_vars.items():
        if not var.get():
            # not selected
            continue
        if file_name in [PROMPT_FILE_NAME, GENERAL_INSTRUCTIONS_FILE_NAME]:
            # skip if special
            continue

        file_path = os.path.join(SOURCE_DIR, file_name)
        file_content = read_text_file(file_path)
        merged_content += f"--- {file_name} ---\n{file_content}\n\n"

    # Merge selected JSON classes
    if os.path.exists(CLASSES_JSON_FILE):
        try:
            with open(CLASSES_JSON_FILE, "r", encoding="utf-8") as f:
                json_data = json.load(f)

            java_kotlin_files = json_data.get("java_kotlin_files", {})

            for package_name, class_dict in java_kotlin_files.items():
                package_checked = json_package_vars[package_name].get() if package_name in json_package_vars else False

                for class_name, class_data in class_dict.items():
                    class_checked = json_class_vars[(package_name, class_name)].get()

                    # If the package is checked OR the class is individually checked, we merge it
                    if package_checked or class_checked:
                        merged_content += (
                            f"--- {class_name} ---\n"
                            f"{json.dumps(class_data, indent=4)}\n\n"
                        )
        except Exception as e:
            messagebox.showerror("Error", f"Could not parse {CLASSES_JSON_FILE_NAME}: {e}")
            return

    # Write everything to merged_output.txt
    write_text_file(OUTPUT_FILE, merged_content)

    # Copy the merged content to the clipboard
    pyperclip.copy(merged_content)

    messagebox.showinfo("Success", "Merged content created and copied to clipboard!")

# --------------------------------------------------------------------
# GUI Setup (Tkinter)
# --------------------------------------------------------------------
def main():
    root = tk.Tk()
    root.title("Flashcard GPT File Merger")
    root.geometry("950x750")

    # 1) Run the script that updates classes.json
    run_get_files_script()

    # ---------------------
    # PROMPT EDITOR SECTION
    # ---------------------
    tk.Label(root, text="Edit 1 prompt.txt:", font=("Arial", 12, "bold")).pack(pady=(10, 0))

    prompt_text = scrolledtext.ScrolledText(root, width=90, height=5, wrap=tk.WORD, font=("Arial", 10))
    prompt_text.pack(pady=5)

    # Load existing "1 prompt.txt" content if available
    if os.path.exists(PROMPT_FILE):
        content = read_text_file(PROMPT_FILE)
        prompt_text.insert(tk.END, content)

    # --------------------------
    # SECOND TEXT FIELD SECTION
    # --------------------------
    tk.Label(root, text="Additional Text (after 1 prompt.txt, before 2 gemeral instructions .txt):",
             font=("Arial", 12, "bold")).pack(pady=(10, 0))

    second_text = scrolledtext.ScrolledText(root, width=90, height=5, wrap=tk.WORD, font=("Arial", 10))
    second_text.pack(pady=5)

    # -----------------------------------------------------
    # FILE SELECTION SECTION (CHECKBOXES IN A SCROLL FRAME)
    # -----------------------------------------------------
    tk.Label(root, text="Select files to merge (excluding 1 prompt & 2 gemeral instructions):",
             font=("Arial", 12, "bold")).pack(pady=(10, 0))

    file_frame = tk.Frame(root, bd=1, relief=tk.SUNKEN)
    file_frame.pack(padx=10, pady=5, fill=tk.BOTH, expand=True)

    # Add a canvas + scrollbar so if there are many files, it can scroll
    file_canvas = tk.Canvas(file_frame)
    scrollbar_files = tk.Scrollbar(file_frame, orient="vertical", command=file_canvas.yview)
    scrollable_files_frame = tk.Frame(file_canvas)

    scrollable_files_frame.bind(
        "<Configure>",
        lambda e: file_canvas.configure(scrollregion=file_canvas.bbox("all"))
    )

    file_canvas.create_window((0, 0), window=scrollable_files_frame, anchor="nw")
    file_canvas.configure(yscrollcommand=scrollbar_files.set)

    file_canvas.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
    scrollbar_files.pack(side=tk.RIGHT, fill=tk.Y)

    # Prepare dictionary for tracking file checkboxes
    file_vars = {}

    # Populate the file checkboxes
    if os.path.exists(SOURCE_DIR):
        valid_files = [
            f for f in os.listdir(SOURCE_DIR)
            if f.endswith((".txt", ".json"))
            and f not in [PROMPT_FILE_NAME, GENERAL_INSTRUCTIONS_FILE_NAME, CLASSES_JSON_FILE_NAME]
        ]
        for vf in valid_files:
            var = tk.BooleanVar(value=False)
            chk = tk.Checkbutton(scrollable_files_frame, text=vf, variable=var, anchor="w")
            chk.pack(fill=tk.X, anchor="w")
            file_vars[vf] = var

    # ----------------------------------------
    # JSON TREEVIEW SECTION (PACKAGES/CLASSES)
    # ----------------------------------------
    tk.Label(root, text="Select JSON Packages & Classes:", font=("Arial", 12, "bold")).pack(pady=(10, 0))

    json_frame = tk.Frame(root, bd=1, relief=tk.SUNKEN)
    json_frame.pack(padx=10, pady=5, fill=tk.BOTH, expand=True)

    json_canvas = tk.Canvas(json_frame)
    scrollbar_json = tk.Scrollbar(json_frame, orient="vertical", command=json_canvas.yview)
    scrollable_json_frame = tk.Frame(json_canvas)

    scrollable_json_frame.bind(
        "<Configure>",
        lambda e: json_canvas.configure(scrollregion=json_canvas.bbox("all"))
    )

    json_canvas.create_window((0, 0), window=scrollable_json_frame, anchor="nw")
    json_canvas.configure(yscrollcommand=scrollbar_json.set)

    json_canvas.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
    scrollbar_json.pack(side=tk.RIGHT, fill=tk.Y)

    # Prepare dicts for JSON packages/classes
    json_package_vars = {}
    json_class_vars = {}

    # Populate checkboxes for packages/classes
    if os.path.exists(CLASSES_JSON_FILE):
        try:
            with open(CLASSES_JSON_FILE, "r", encoding="utf-8") as f:
                data = json.load(f)

            java_kotlin_files = data.get("java_kotlin_files", {})
            for package_name, classes_dict in java_kotlin_files.items():
                pkg_var = tk.BooleanVar(value=False)
                # Package-level checkbox
                pkg_chk = tk.Checkbutton(scrollable_json_frame, text=f"[{package_name}]", variable=pkg_var, anchor="w")
                pkg_chk.pack(fill=tk.X, anchor="w")
                json_package_vars[package_name] = pkg_var

                # Indent classes
                for class_name in classes_dict.keys():
                    cls_var = tk.BooleanVar(value=False)
                    cls_chk = tk.Checkbutton(scrollable_json_frame, text=f"   - {class_name}",
                                             variable=cls_var, anchor="w")
                    cls_chk.pack(fill=tk.X, anchor="w")
                    json_class_vars[(package_name, class_name)] = cls_var

        except Exception as e:
            messagebox.showerror("Error", f"Could not read {CLASSES_JSON_FILE_NAME}: {e}")
    else:
        messagebox.showwarning("No JSON Found", f"No {CLASSES_JSON_FILE_NAME} found.")

    # -------------------------
    # MERGE BUTTON
    # -------------------------
    def on_merge_button_clicked():
        merge_and_copy_to_clipboard(
            second_text_content=second_text.get("1.0", tk.END),
            file_vars=file_vars,
            json_package_vars=json_package_vars,
            json_class_vars=json_class_vars,
            prompt_text_widget=prompt_text
        )

    merge_button = tk.Button(root, text="Merge & Copy", command=on_merge_button_clicked,
                             font=("Arial", 12), bg="lightblue")
    merge_button.pack(pady=10)

    root.mainloop()

if __name__ == "__main__":
    main()

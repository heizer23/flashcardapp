import tkinter as tk
from tkinter import messagebox, scrolledtext
import os
import subprocess  # To run getFiles.py
import sys
import json  # For JSON parsing
import pyperclip  # Install via `pip install pyperclip`

# Get the base directory (folder containing this script)
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

# Define file paths using relative paths
SOURCE_DIR = os.path.join(BASE_DIR, "copied")
PROMPT_FILE = os.path.join(SOURCE_DIR, "1 prompt.txt")
OUTPUT_FILE = os.path.join(SOURCE_DIR, "merged_output.txt")
CLASSES_JSON_FILE = os.path.join(SOURCE_DIR, "classes.json")
GET_FILES_SCRIPT = os.path.join(BASE_DIR, "getFiles.py")  # Ensure correct path

# Function to run getFiles.py before loading JSON
def update_classes_json():
    if os.path.exists(GET_FILES_SCRIPT):
        try:
            print(f"Running {GET_FILES_SCRIPT}...")
            result = subprocess.run([sys.executable, GET_FILES_SCRIPT], capture_output=True, text=True, check=True)
            print("getFiles.py output:", result.stdout)
            print("getFiles.py errors:", result.stderr)
        except subprocess.CalledProcessError as e:
            messagebox.showerror("Error", f"Failed to update classes.json: {e}\n{e.stderr}")

# Function to load JSON structure
def load_json_structure():
    json_tree.delete(0, tk.END)
    if os.path.exists(CLASSES_JSON_FILE):
        try:
            with open(CLASSES_JSON_FILE, "r", encoding="utf-8") as f:
                data = json.load(f)
                if "java_kotlin_files" in data:
                    for package_name, classes in data["java_kotlin_files"].items():
                        json_tree.insert(tk.END, f"[{package_name}]")  # Package level
                        for class_name in classes.keys():
                            json_tree.insert(tk.END, f"  - {class_name}")  # Class level
        except Exception as e:
            messagebox.showerror("Error", f"Could not read classes.json: {e}")
    else:
        messagebox.showwarning("No JSON Found", "No classes.json found in the directory.")

# Function to merge selected files and JSON classes
def merge_files():
    selected_files = [file_listbox.get(i) for i in file_listbox.curselection()]
    selected_json_items = [json_tree.get(i) for i in json_tree.curselection()]

    if not selected_files and not selected_json_items:
        messagebox.showwarning("No Selection", "Please select at least one file or JSON class.")
        return

    merged_content = ""

    # Save changes to "1 prompt.txt" before merging
    try:
        with open(PROMPT_FILE, "w", encoding="utf-8") as f:
            f.write(prompt_text.get("1.0", tk.END).strip())
    except Exception as e:
        messagebox.showerror("Error", f"Could not save 1 prompt.txt: {e}")
        return

    # Ensure "1 prompt.txt" is always included
    if "1 prompt.txt" not in selected_files:
        selected_files.insert(0, "1 prompt.txt")

    # Merge selected text files
    for file in selected_files:
        file_path = os.path.join(SOURCE_DIR, file)
        try:
            with open(file_path, "r", encoding="utf-8") as f:
                merged_content += f"--- {file} ---\n{f.read()}\n\n"
        except Exception as e:
            messagebox.showerror("Error", f"Could not read {file}: {e}")
            return

    # Merge selected JSON classes
    try:
        with open(CLASSES_JSON_FILE, "r", encoding="utf-8") as f:
            json_data = json.load(f)

            # Handle JSON selections
            for item in selected_json_items:
                if item.startswith("[") and item.endswith("]"):  # Top-level package selected
                    package_name = item[1:-1]
                    if package_name in json_data["java_kotlin_files"]:
                        for class_name, class_data in json_data["java_kotlin_files"][package_name].items():
                            merged_content += f"--- {class_name} ---\n{json.dumps(class_data, indent=4)}\n\n"
                elif item.startswith("  - "):  # Individual class selected
                    class_name = item[4:].strip()
                    for package_name, classes in json_data["java_kotlin_files"].items():
                        if class_name in classes:
                            merged_content += f"--- {class_name} ---\n{json.dumps(classes[class_name], indent=4)}\n\n"
    except Exception as e:
        messagebox.showerror("Error", f"Could not read JSON data: {e}")
        return

    # Save merged content to a new text file
    try:
        with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
            f.write(merged_content)
    except Exception as e:
        messagebox.showerror("Error", f"Could not write to output file: {e}")
        return

    # Copy merged content to clipboard
    pyperclip.copy(merged_content)

    messagebox.showinfo("Success", "Files and JSON data merged and copied to clipboard!")

# GUI Setup
root = tk.Tk()
root.title("Flashcard GPT File Merger")
root.geometry("750x600")

# Run the update script before listing files
update_classes_json()

# Label for editing prompt
tk.Label(root, text="Edit 1 prompt.txt:", font=("Arial", 12)).pack(pady=5)

# Text widget to edit "1 prompt.txt"
prompt_text = scrolledtext.ScrolledText(root, width=80, height=5, wrap=tk.WORD, font=("Arial", 10))
prompt_text.pack(pady=5)

# Load existing "1 prompt.txt" content if available
if os.path.exists(PROMPT_FILE):
    try:
        with open(PROMPT_FILE, "r", encoding="utf-8") as f:
            prompt_text.insert(tk.END, f.read())
    except Exception as e:
        messagebox.showerror("Error", f"Could not load 1 prompt.txt: {e}")

# Label for file selection
tk.Label(root, text="Select files to merge:", font=("Arial", 12)).pack(pady=10)

# Listbox for file selection
file_listbox = tk.Listbox(root, selectmode=tk.MULTIPLE, width=80, height=7)
file_listbox.pack(pady=5)

# Populate listbox with .txt and .json files (excluding "1 prompt.txt" and "classes.json")
if os.path.exists(SOURCE_DIR):
    text_files = [f for f in os.listdir(SOURCE_DIR) if f.endswith((".txt", ".json")) and f not in ["1 prompt.txt", "classes.json"]]
    for file in text_files:
        file_listbox.insert(tk.END, file)

# Label for JSON class selection
tk.Label(root, text="Select JSON Packages & Classes:", font=("Arial", 12)).pack(pady=10)

# Listbox for JSON selection
json_tree = tk.Listbox(root, selectmode=tk.MULTIPLE, width=80, height=10)
json_tree.pack(pady=5)

# Load JSON structure
load_json_structure()

# Button to merge files and JSON
merge_button = tk.Button(root, text="Merge & Copy", command=merge_files, font=("Arial", 12), bg="lightblue")
merge_button.pack(pady=10)

# Run GUI loop
root.mainloop()

import tkinter as tk
from tkinter import messagebox
import os
import subprocess  # To run getFiles.py
import sys
import pyperclip  # Install via `pip install pyperclip`

# Define file paths
SOURCE_DIR = r"C:\Users\Linse\Documents\Programmieren\flashcardapp\GptPipeline\copied"
OUTPUT_FILE = os.path.join(SOURCE_DIR, "merged_output.txt")
GET_FILES_SCRIPT = os.path.join(os.path.dirname(__file__), "getFiles.py")  # Ensure correct path

# Function to ensure getFiles.py runs before listing files
def update_classes_json():
    if os.path.exists(GET_FILES_SCRIPT):
        try:
            print(f"Running {GET_FILES_SCRIPT}...")  # Debugging
            result = subprocess.run([sys.executable, GET_FILES_SCRIPT], capture_output=True, text=True, check=True)
            print("getFiles.py output:", result.stdout)  # Debugging
            print("getFiles.py errors:", result.stderr)  # Debugging
        except subprocess.CalledProcessError as e:
            messagebox.showerror("Error", f"Failed to update classes.json: {e}\n{e.stderr}")
            print(f"Error running getFiles.py: {e}")  # Debugging
    else:
        messagebox.showerror("Error", f"getFiles.py not found: {GET_FILES_SCRIPT}")
        print(f"getFiles.py not found: {GET_FILES_SCRIPT}")  # Debugging

# Function to merge files
def merge_files():
    selected_files = [file_listbox.get(i) for i in file_listbox.curselection()]
    
    if not selected_files:
        messagebox.showwarning("No Selection", "Please select at least one file.")
        return
    
    merged_content = ""

    for file in selected_files:
        file_path = os.path.join(SOURCE_DIR, file)
        try:
            with open(file_path, "r", encoding="utf-8") as f:
                merged_content += f"--- {file} ---\n{f.read()}\n\n"  # Separate files with headers
        except Exception as e:
            messagebox.showerror("Error", f"Could not read {file}: {e}")
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
    
    messagebox.showinfo("Success", "Files merged and copied to clipboard!")

# GUI Setup
root = tk.Tk()
root.title("Flashcard GPT File Merger")
root.geometry("500x400")

# Run the update script before listing files
update_classes_json()

# Label
tk.Label(root, text="Select files to merge:", font=("Arial", 12)).pack(pady=10)

# Listbox for file selection
file_listbox = tk.Listbox(root, selectmode=tk.MULTIPLE, width=60, height=10)
file_listbox.pack(pady=10)

# Populate listbox with .txt and .json files
if os.path.exists(SOURCE_DIR):
    text_files = [f for f in os.listdir(SOURCE_DIR) if f.endswith((".txt", ".json"))]
    if text_files:
        for file in text_files:
            file_listbox.insert(tk.END, file)
    else:
        messagebox.showwarning("No Files Found", "No .txt or .json files found in the directory.")
else:
    messagebox.showerror("Error", f"Directory not found: {SOURCE_DIR}")

# Button to merge files
merge_button = tk.Button(root, text="Merge & Copy", command=merge_files, font=("Arial", 12), bg="lightblue")
merge_button.pack(pady=10)

# Run GUI loop
root.mainloop()

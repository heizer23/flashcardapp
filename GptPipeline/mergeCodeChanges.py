import os
import json
from pathlib import Path

def merge_code_changes():
    """
    Reads a GPT response JSON (with 'explanation' and 'code_changes' top-level keys)
    from 'copied/answer.json' (relative to this script), then applies the changes
    to your local codebase by overwriting, creating, or deleting files as specified.
    """
    
    # Determine where this script is located
    script_dir = Path(__file__).resolve().parent
    
    # Build a path to the 'answer.json' in 'copied/' relative to this script
    json_path = script_dir / "copied" / "answer.json"
    
    # Make sure the file exists
    if not json_path.is_file():
        print(f"ERROR: Could not find the file at {json_path}. Please confirm the path.")
        return
    
    # Load the JSON
    with open(json_path, "r", encoding="utf-8") as f:
        data = json.load(f)
    
    # The script expects a structure like:
    # {
    #   "explanation": { ... },
    #   "code_changes": {
    #       "com.example.somepackage": {
    #           "SomeClass": {
    #               "file_path": "...",
    #               "content": "...",
    #               "changes": "update" | "create" | "delete"
    #           },
    #           ...
    #       },
    #       ...
    #   }
    # }
    
    code_changes = data.get("code_changes", {})
    
    # Iterate over packages -> classes
    for package_name, class_dict in code_changes.items():
        for class_name, class_info in class_dict.items():
            file_path = class_info.get("file_path")
            content = class_info.get("content", "")
            change_type = class_info.get("changes")
            
            target_path = Path(file_path)
            
            if change_type in ["update", "create"]:
                # Ensure parent directories exist
                os.makedirs(target_path.parent, exist_ok=True)
                
                # Write new content (overwrites existing file if "update", or creates a new one)
                with open(target_path, "w", encoding="utf-8") as f_out:
                    f_out.write(content)
                
                print(f"{change_type.upper()}: {file_path}")
            
            elif change_type == "delete":
                # Delete the file if it exists
                if target_path.is_file():
                    target_path.unlink()
                    print(f"DELETE: {file_path}")
                else:
                    print(f"WARNING: File to delete not found: {file_path}")
            else:
                # If no recognized change type, just skip
                print(f"SKIP (unrecognized 'changes' type) for: {file_path}")

if __name__ == "__main__":
    merge_code_changes()

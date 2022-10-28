## Overview
The JPortal2 VS Code extension is available on from the VSCode extensions tab.

To install the JPortal extension, open VSCode, then choose the the Extensions tab on the left (Or press Ctrl-P and type "install extensions"). Type "JPortal"
into the search field, choose the JPortal extension, and install it. See the video below:

![Installing the JPortal extension](../img/installing-jportal-in-vscode.gif)

It is on the todo list to have the VSCode plugin do a compile task, but for now, you can set up VSCode to compile your si, using a tasks.json file
inside the .vscode directory in your project. Once set up, you can press Ctrl-Shift-B to run, or set it up with other keys.

Instructions on how to set up tasks in VSCode are [here](https://code.visualstudio.com/docs/editor/tasks)

Below is an example tasks.json file that will work for this demo:

```json
{
  // See https://go.microsoft.com/fwlink/?LinkId=733558
  // for the documentation about the tasks.json format
  "version": "2.0.0",
  "tasks": [
    {
      "label": "JPortal2",
      "type": "shell",
      "command": "./generate_jportal.sh",
      "windows": {
        "command": "wsl .\\generate_jportal.sh"
      },
      "options": {
        "cwd": "${workspaceFolder} "
      },
      "problemMatcher": [],
      "group": {
        "kind": "build",
        "isDefault": true
      }
    }
  ]
}
```

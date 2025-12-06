# VSCode development Setup

This document explains the setup process when opening the project in VSCode.


```plantuml
@startuml VSCode_Startup_Sequence

title VSCode Startup Sequence for Laminar Form Derivation

actor User
participant "VSCode" as vscode
participant "Task: demo" as demo
participant "Task: setup" as setup
participant "Task: runDemo" as runDemo
participant "Task: fastLink" as fastLink
participant "Task: npmDev" as npmDev
participant "setup.sc" as setupScript
participant "fastLink.sh" as fastLinkScript
participant "npmDev.sh" as npmDevScript

User -> vscode: Opens project folder
vscode -> demo: Triggers "demo" task\n(runOn: folderOpen)
demo -> setup: Executes "setup" task\n(sequence)
demo -> runDemo: Executes "runDemo" task\n(sequence)

setup -> setupScript: Runs ./scripts/setup.sc
setupScript -> setupScript: Checks if build-env.sh needs regeneration
setupScript -> setupScript: Checks if node modules need installation
setupScript --> setup: Returns completion

runDemo -> fastLink: Executes "fastLink" task\n(parallel)
runDemo -> npmDev: Executes "npmDev" task\n(parallel)

fastLink -> fastLinkScript: Runs ./scripts/fastLink.sh
fastLinkScript -> fastLinkScript: Waits for npm dev server marker
fastLinkScript -> fastLinkScript: Waits for main.js generation
fastLinkScript -> fastLinkScript: Runs sbt ~client/fastLinkJS
fastLinkScript --> fastLink: Returns completion

npmDev -> npmDevScript: Runs ./scripts/npmDev.sh
npmDevScript -> npmDevScript: Creates npm dev server marker
npmDevScript -> npmDevScript: Runs npm run dev in client
npmDevScript --> npmDev: Returns completion

@enduml

```


The sequence diagram shows:

When VSCode opens the project folder, it automatically triggers the "demo" task


The "demo" task runs the "setup" task first, then the "runDemo" task
* The "setup" task runs the Scala script that checks build environment and node modules
* The "runDemo" task runs "fastLink" and "npmDev" tasks in parallel
  * "fastLink" waits for the npm dev server to start and then runs the Scala.js fastLink compilation
  * "npmDev" starts the npm development server for the client

The key files involved are:

* .vscode/tasks.json - Defines the task dependencies and execution order
* scripts/setup.sc - Handles project setup and environment checks
* scripts/fastLink.sh - Manages the Scala.js compilation process
* scripts/npmDev.sh - Starts the npm development server
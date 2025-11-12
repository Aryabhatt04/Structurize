Structurize 🚀

Structurize is an interactive, animated desktop application designed to help computer science students see, understand, and interact with fundamental data structures and algorithms.

Built with modern Java 23 and JavaFX, it provides a clean, tabbed interface to demystify complex concepts like recursion and pointer manipulation by turning them into dynamic, easy-to-understand visualizations.

Structurize in Action


✨ Features

🖥️ Modern Tabbed Interface: Open and compare multiple data structures in a single window.

🎨 Clean, Dark-Theme UI: Styled with a single, modern CSS file for a responsive and beautiful user experience.

🎬 Real-Time Animations: Watch every operation (push, pop, insert, shift) animate in real-time.

🏛️ Strict MVC Architecture: A clean, decoupled codebase separating logic (Model), UI (View), and controls (Controller).

🗃️ Database Logging: All operations are logged to a local SQLite database for review.

⚡ Interactive Graph: Click and drag graph nodes; edges update automatically!

📊 Visualizations Included

Stack (LIFO)

Queue (FIFO)

Singly Linked List

Binary Search Tree (with In/Pre/Post-Order Traversals)

Directed Graph (with BFS & DFS Traversals)

🛠️ Tech Stack

Core: Java 23

GUI: JavaFX 23 (with FXML for layouts)

Styling: CSS 3

Database: SQLite (using sqlite-jdbc)

Build/Packaging: Gradle and jpackage (for creating the native installer)

🏗️ Architecture: The MVC Pattern

This project is built on a strict Model-View-Controller (MVC) pattern. This was a core design goal to ensure the application is maintainable, testable, and extensible.

Model: (The model package) Pure Java logic. Contains no javafx imports. Manages the data and rules.

View: (The fxml files & styles.css) Declarative FXML files for layout and a CSS stylesheet for styling. Contains no business logic.

Controller: (The controller package) The "bridge" that handles FXML events, calls the Model, and triggers animations in the View.

Note: Replace the link above with an uploaded version of the System Architecture Diagram (Figure 3.1) from your project report!



🔮 Future Scope

The MVC architecture makes it easy to add new features. Future plans include:

Advanced Algorithms: Implement Dijkstra's, MST, and the BST deleteNode operation.

More Structures: Add AVL Trees (with balancing animations) and Heaps.

Playback Controls: Add a speed slider, pause/play, and step-forward buttons.

Pseudo-code Panel: Display the code for the algorithm as it runs and highlight the current line.

Save/Load State: Save a complex tree or graph to a file and load it back.

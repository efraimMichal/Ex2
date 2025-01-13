# README: Spreadsheet Application

## Overview
This spreadsheet tool is built to provide essential functionality for organizing and computing data in a tabular format.
It supports storing text, numbers, and formulas, making it an ideal solution for handling small to medium-scale data tasks.

---

## Features
- *Flexible Grid*:
  - Customize spreadsheet dimensions to suit your data requirements.
  - Navigate cells using standard references like A1 or B3.

- *Advanced Cell Operations*:
  - Input text, numeric values, or formulas directly into cells.
  - Automatically calculate results from formulas referencing other cells.

- *Error and Dependency Management*:
  - Identify and resolve issues caused by circular references.
  - Handle incorrect formula syntax gracefully.

- *File Handling*:
  - Save the current state of the spreadsheet for future use.
  - Reload and continue from saved spreadsheets.

- *User-Friendly Interface*:
  - Graphical environment for visualizing and interacting with the spreadsheet.

<img width="1190" alt="image" src="https://github.com/user-attachments/assets/a060f7ee-d575-476e-b9c9-6dc8134f3844" />


---

## Key Components
1. *SCell*:
   - Represents a single cell, storing its data and managing computations for formulas.

2. *Ex2Sheet*:
   - Oversees the spreadsheet as a whole, linking and managing interactions between cells.

3. *CellEntry & Index2D*:
   - Provide utility support for translating cell references to grid positions.

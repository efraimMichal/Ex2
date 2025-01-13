package firstPart;

/**
 * Represents a cell in a spreadsheet. A cell can store text, numbers, or formulas.
 */
/**
 * Represents a cell in a spreadsheet. A cell can store text, numbers, or formulas.
 */
public class Cell {
    private String content; // The content of the cell
    private Spreadsheet spreadsheet; // Reference to the spreadsheet the cell belongs to

    public Cell(String content, Spreadsheet spreadsheet) {
        this.content = content; // Initialize content
        this.spreadsheet = spreadsheet; // Set spreadsheet reference
    }

    public String getContent() {
        return content; // Return cell content
    }

    public void setContent(String content) {
        this.content = content; // Update cell content
    }

    /**
     * Checks if the given value is a valid number.
     */
    public boolean isNumber(String value) {
        if (value == null || value.isEmpty()) {
            return false; // Null or empty strings are not numbers
        }
        try {
            Double.parseDouble(value); // Try parsing the value as a double
            return true; // Parsing succeeded, it's a number
        } catch (NumberFormatException e) {
            return false; // Parsing failed, not a number
        }
    }

    /**
     * Checks if the content is plain text.
     */
    public boolean isText(String text) {
        return !(isNumber(text) || isForm(text)); // Content is text if not a number or formula
    }

    /**
     * Validates if the given input is a formula.
     */
    public boolean isForm(String input) {
        if (input == null || !input.startsWith("=")) {
            return false; // Formulas must start with '='
        }

        String formula = input.substring(1).replaceAll("\\s", ""); // Remove '=' and spaces
        if (formula.isEmpty() || formula.contains("()")) {
            return false; // Empty formulas or empty parentheses are invalid
        }

        int balance = 0; // Parentheses balance tracker
        for (char ch : formula.toCharArray()) {
            if (ch == '(') balance++;
            if (ch == ')') balance--;
            if (balance < 0) return false; // More closing than opening parentheses
            if (!Character.isDigit(ch) && "+-*/().".indexOf(ch) == -1 && !Character.isLetterOrDigit(ch)) {
                return false; // Invalid characters
            }
        }
        return balance == 0; // Parentheses must be balanced
    }

    /**
     * Resolves the value of the cell by evaluating its content.
     */
    public String resolveValue() {
        if (isNumber(content)) {
            return content; // Return content directly if it's a number
        } else if (isForm(content)) {
            return String.valueOf(computeForm(content)); // Evaluate formula
        } else {
            throw new IllegalStateException("Cannot resolve non-numeric, non-formula value: " + content);
        }
    }

    /**
     * Computes the result of a formula.
     */
    public Double computeForm(String form) {
        if (!isForm(form)) {
            throw new IllegalArgumentException("Invalid formula"); // Validate formula
        }
        return evaluate(form.substring(1).replaceAll("\\s", "")); // Evaluate formula
    }

    /**
     * Evaluates an expression, handling parentheses and simple arithmetic.
     */
    private Double evaluate(String expression) {
        expression = expression.replaceAll("\\s", ""); // Remove spaces
        int openParenIndex = expression.lastIndexOf('(');
        while (openParenIndex != -1) {
            int closeParenIndex = expression.indexOf(')', openParenIndex);
            if (closeParenIndex == -1) {
                throw new IllegalArgumentException("Unmatched parentheses in expression");
            }

            // Evaluate innermost parentheses
            String innerExpression = expression.substring(openParenIndex + 1, closeParenIndex);
            Double innerResult = evaluate(innerExpression);

            // Replace parentheses with evaluated result
            expression = expression.substring(0, openParenIndex) + innerResult + expression.substring(closeParenIndex + 1);
            openParenIndex = expression.lastIndexOf('(');
        }
        return evaluateSimpleExpression(expression); // Evaluate remaining expression
    }

    /**
     * Evaluates a simple arithmetic expression without parentheses.
     */
    private Double evaluateSimpleExpression(String expression) {
        double result = 0;
        double currentNumber = 0;
        char operator = '+'; // Default operator

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                StringBuilder number = new StringBuilder();
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    number.append(expression.charAt(i++)); // Build number
                }
                i--;
                currentNumber = Double.parseDouble(number.toString()); // Parse number
            } else if (Character.isLetter(c)) {
                StringBuilder cellRef = new StringBuilder();
                while (i < expression.length() && Character.isLetterOrDigit(expression.charAt(i))) {
                    cellRef.append(expression.charAt(i++)); // Build cell reference
                }
                i--;
                currentNumber = getCellValue(cellRef.toString()); // Get referenced cell value
            } else if ("+-*/".indexOf(c) >= 0) {
                // Apply the previous operator to the result and current number
                switch (operator) {
                    case '+': result += currentNumber; break;
                    case '-': result -= currentNumber; break;
                    case '*': result *= currentNumber; break;
                    case '/':
                        if (currentNumber == 0) {
                            throw new ArithmeticException("Division by zero");
                        }
                        result /= currentNumber; break;
                }
                operator = c; // Update operator
                currentNumber = 0; // Reset current number
            } else {
                throw new IllegalArgumentException("Invalid character in expression: " + c);
            }
        }

        // Apply the last operator
        switch (operator) {
            case '+': result += currentNumber; break;
            case '-': result -= currentNumber; break;
            case '*': result *= currentNumber; break;
            case '/':
                if (currentNumber == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                result /= currentNumber; break;
        }

        return result; // Return final result
    }

    /**
     * Retrieves the value of a referenced cell.
     */
    private double getCellValue(String cellReference) {
        int col = cellReference.charAt(0) - 'A'; // Column index
        int row = Integer.parseInt(cellReference.substring(1)) - 1; // Row index
        String value = spreadsheet.eval(col, row); // Evaluate cell value
        if (value == null) {
            throw new IllegalArgumentException("Cell " + cellReference + " is empty or invalid");
        }
        return Double.parseDouble(value); // Parse value as double
    }
}
package assignments.ex2;

/**
 * Implementation of SCell class, representing a single cell in the spreadsheet.
 */
public class SCell implements Cell {
    private String line; // Raw content of the cell
    private int type; // Type of the cell (e.g., FORM, TEXT, NUMBER)
    private int order; // Order of evaluation for the cell
    private String computedValue; // Computed value of the cell if it's a formula

    /**
     * Constructor to initialize the cell with a given input string.
     * @param s Input string to initialize the cell.
     */
    public SCell(String s) {
        setData(s);
    }

    /**
     * Gets the raw data of the cell.
     * @return Raw string content of the cell.
     */
    @Override
    public String getData() {
        return line;
    }

    /**
     * Sets the data for the cell and determines its type based on the input.
     * @param s New data to set for the cell.
     */
    @Override
    public void setData(String s) {
        line = s;
        computedValue = null; // Reset computed value
        if (s.startsWith("=")) {
            if (isValidFormula(s)) {
                type = Ex2Utils.FORM; // Valid formula
            } else {
                type = Ex2Utils.ERR_FORM_FORMAT; // Invalid formula
                computedValue = Ex2Utils.ERR_FORM;
            }
        } else if (isNumber(s)) {
            type = Ex2Utils.NUMBER; // Numeric content
            computedValue = s;
        } else {
            type = Ex2Utils.TEXT; // Text content
        }
    }

    /**
     * Checks if a given string represents a valid number.
     * @param s String to check.
     * @return True if the string is a valid number, otherwise false.
     */
    private boolean isNumber(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates whether the input string is a valid formula.
     * @param s Formula string to validate.
     * @return True if the formula is valid, otherwise false.
     */
    private boolean isValidFormula(String s) {
        String formula = s.substring(1).replaceAll("\\s", "").toUpperCase(); // Normalize to uppercase

        if (formula.isEmpty() || formula.contains("()")) {
            return false;
        }

        if (formula.matches(".*[+\\-*/]{2,}.*") || formula.endsWith("+")) {
            return false; // Invalid operators
        }

        int balance = 0;
        for (char c : formula.toCharArray()) {
            if (c == '(') {
                balance++;
            } else if (c == ')') {
                balance--;
                if (balance < 0) {
                    return false;
                }
            }
        }

        return balance == 0 && formula.matches("([A-Z]+\\d+|\\d+\\.\\d+|\\d+|\\(.*\\))([+\\-*/]([A-Z]+\\d+|\\d+\\.\\d+|\\d+|\\(.*\\)))*");
    }

    /**
     * Computes the value of the cell if it's a formula.
     * @param sheet Reference to the spreadsheet for resolving cell references.
     */
    public void computeValue(Ex2Sheet sheet) {
        if (type != Ex2Utils.FORM) {
            return; // Not a formula, no computation needed
        }

        try {
            String formula = line.substring(1).replaceAll("\\s", "").toUpperCase(); // Normalize formula
            computedValue = String.valueOf(evaluateFormula(formula, sheet));
        } catch (Exception e) {
            type = Ex2Utils.ERR_FORM_FORMAT; // Invalid formula computation
            computedValue = Ex2Utils.ERR_FORM;
        }
    }

    /**
     * Evaluates a formula string, handling nested parentheses.
     * @param formula Formula string to evaluate.
     * @param sheet Reference to the spreadsheet.
     * @return Computed result of the formula.
     */
    private double evaluateFormula(String formula, Ex2Sheet sheet) {
        while (formula.contains("(")) {
            int openIndex = formula.lastIndexOf('(');
            int closeIndex = formula.indexOf(')', openIndex);
            if (closeIndex == -1) {
                throw new IllegalArgumentException("Unmatched parentheses in formula");
            }
            String subExpression = formula.substring(openIndex + 1, closeIndex);
            double subResult = evaluateSimpleFormula(subExpression, sheet);
            formula = formula.substring(0, openIndex) + subResult + formula.substring(closeIndex + 1);
        }
        return evaluateSimpleFormula(formula, sheet);
    }

    /**
     * Evaluates a simple formula without parentheses.
     * @param formula Formula string to evaluate.
     * @param sheet Reference to the spreadsheet.
     * @return Computed result of the formula.
     */
    private double evaluateSimpleFormula(String formula, Ex2Sheet sheet) {
        double result = 0;
        double currentNumber = 0;
        char lastOperator = '+';
        boolean hasNumber = false;

        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                // Accumulate number
                StringBuilder number = new StringBuilder();
                while (i < formula.length() && (Character.isDigit(formula.charAt(i)) || formula.charAt(i) == '.')) {
                    number.append(formula.charAt(i++));
                }
                i--;
                currentNumber = Double.parseDouble(number.toString());
                hasNumber = true;
            } else if (Character.isLetter(c)) {
                // Handle cell references
                StringBuilder cellRef = new StringBuilder();
                while (i < formula.length() && Character.isLetterOrDigit(formula.charAt(i))) {
                    cellRef.append(formula.charAt(i++));
                }
                i--;
                String normalizedCellRef = cellRef.toString().toUpperCase();
                Cell cell = sheet.get(normalizedCellRef);
                if (cell == null || !(cell instanceof SCell)) {
                    throw new IllegalArgumentException("Invalid cell reference: " + cellRef);
                }
                String value = ((SCell) cell).computedValue;
                if (value == null || !isNumber(value)) {
                    throw new IllegalArgumentException("Referenced cell is invalid: " + normalizedCellRef);
                }
                currentNumber = Double.parseDouble(value);
                hasNumber = true;
            } else if (c == '+' || c == '-' || c == '*' || c == '/') {
                // Apply previous operation
                result = applyOperation(lastOperator, result, currentNumber);
                lastOperator = c;
                currentNumber = 0;
            }
        }

        if (hasNumber) {
            result = applyOperation(lastOperator, result, currentNumber);
        }

        return result;
    }

    /**
     * Applies a mathematical operation between two numbers.
     * @param op Operator character (e.g., '+', '-', '*', '/').
     * @param a First operand.
     * @param b Second operand.
     * @return Result of the operation.
     */
    private double applyOperation(char op, double a, double b) {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/':
                if (b == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return a / b;
            default: throw new IllegalArgumentException("Invalid operator: " + op);
        }
    }

    /**
     * Sets the computed value of the cell.
     * @param value Computed value to set.
     */
    public void setComputedValue(String value) {
        computedValue = value;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int t) {
        type = t;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int t) {
        order = t;
    }

    @Override
    public String toString() {
        return (computedValue != null) ? computedValue : line;
    }
}
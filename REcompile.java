/**
 * Jayden Litolff
 * 1614273
 * Modified May 2025
 * Built off of the week 7 lecture examples
 * 
 * Returning 0 is used to mean error in expression, term and factor as nothing
 * will ever point back at the dummy start state
 */

public class REcompile {

    public static void main(String[] args) {

        // Read from stdin
        if (args.length != 1) {
            System.out.println("Invalid Arguments: Usage java REcompile regex");
            return;
        }

        // Compile the regex
        REcompile compiledRegex = new REcompile(args[0]);

        // write to stdout
        System.out.println(compiledRegex);
    }

    // The regex to compile
    char[] pattern;

    // Where we are in the regex pattern
    int j = 0;

    // The compiled regex state data
    int state = 1;
    String[] type = new String[100];
    int[] next1 = new int[100];
    int[] next2 = new int[100];

    public REcompile(String pattern) {
        // Make the string easier to work with
        this.pattern = pattern.toCharArray();
        // Build an expression
        int r = expression();
        // Handle Errors, including unmatched ')'
        if (r < 1) {
            state = -1;
            return;
        }
        // Point the entry state dummy at the expressions entrypoint
        setstate(0, "BR", r, r);
    }

    /**
     * Build an expression of terms
     * Handles concatenation
     * 
     * @return int the start state of an entire machine / expression
     */
    int expression() {
        // Match a term
        int r = term();
        // Handle errors in term
        if (r == 0)
            return 0;

        // Handle end of pattern
        if (j == pattern.length) {
            // set final state of the machine indicating end of machine
            setstate(state, "BR", -1, -1);
            // return the start state of the expression
            return r;
        }
        // Handle end of expression
        if (pattern[j] == ')') {
            // return the start state of the expression, negative to indicate ending at a ')'
            return -r;
        }
        // Concatenation
        if (inVocab(pattern[j]) || pattern[j] == '(' || pattern[j] == '.' || pattern[j] == '\\') {
            // Save the location of the terms end state
            int termLast = state - 1;
            // build a machine at the end of term r's machine
            int r2 = expression();
            // since r2 is not the original expression() call, negative (i.e. it finished at
            // a ')') is just flipped to positive
            if (r2 < 0)
                r2 = -r2;
            // Handle errors
            if (r2 == 0)
                return 0;
            // Point the final state of r's machine at the starting state of r2's machine
            if (type[termLast].equals("BR")) {
                // Final branching machines always use next2 as the pointer to move on
                setstate(termLast, "BR", next1[termLast], r2);
            } else {
                setstate(termLast, type[termLast], r2, r2);
            }
            // return the start state of the expression
            return r;
        }
        // Error, as not at end of expression, not at end of pattern and not in
        // concatenation
        return 0;
    }

    /**
     * Builds a term of factors
     * handles all closure types and alternation
     * 
     * @return int start state of a machine
     */
    int term() {
        // Match a factor
        int f = factor();

        // Handle errors
        if (f == 0)
            return 0;

        // Handle end of pattern
        if (j == pattern.length)
            return f;

        // Closure - Zero or More
        if (pattern[j] == '*') {
            // branching machine, pointing at found factor f or next state after machine
            setstate(state, "BR", f, state + 1);
            // consume char
            state++;
            j++;
            // Return branching machine as entrypoint
            return state - 1;
        }

        // Closure - Zero or One
        if (pattern[j] == '?') {
            // branching machine, pointing at found factor f or next state after machine
            setstate(state, "BR", f, state + 1);
            // point the found factor at the exit instead of the branching machine
            setstate(state - 1, type[state - 1], state + 1, state + 1);
            // consume char
            state++;
            j++;
            // return branching machine as entrypoint
            return state - 1;
        }

        // Closure - One or More
        if (pattern[j] == '+') {
            // branching machine, pointing at factor f or next state after machine
            setstate(state, "BR", f, state + 1);
            // consume char
            state++;
            j++;
            // return the found factor as entrypoint
            return f;
        }

        // Disjunction / Alternation
        if (pattern[j] == '|') {
            // store the start and end of the machine before the disjunction
            int f1 = f;
            int f1last = state - 1;
            // consume the '|'
            j++;
            // store where the branch machine will be assembled
            f = state;
            state++;
            // make the machine after the disjunction character
            int f2 = term();
            // Handle errors
            if (f2 == 0)
                return 0;
            // redirect end of first machine
            if (type[f1last].equals("BR")) {
                setstate(f1last, "BR", next1[f1last], state);
            } else {
                setstate(f1last, type[f1last], state, state);
            }
            // setup branch machine into the f1 and f2
            setstate(f, "BR", f1, f2);
            // return the branch machine
            return f;
        }

        return f;
    }

    /**
     * Builds a factor
     * handles escape and wildcard chars
     * 
     * @return int the start state of the factor
     */
    int factor() {

        // Can't match beyond end of pattern
        if (j == pattern.length)
            return 0;

        // Literals
        if (inVocab(pattern[j])) {
            // build the machine
            setstate(state, "" + pattern[j], state + 1, state + 1);
            // consume the character
            j++;
            state++;
            // return location of the machine
            return state - 1;
        }

        // Escape character
        if (pattern[j] == '\\') {
            // Consume the escape char
            j++;
            // Can't match beyond end of pattern
            if (j == pattern.length)
                return 0;
            // build the machine using the escaped character
            setstate(state, "" + pattern[j], state + 1, state + 1);
            // consume the escaped character
            j++;
            state++;
            // return location of the machine
            return state - 1;
        }

        // Wildcard matching any literal
        if (pattern[j] == '.') {
            // build the wildcard machine
            setstate(state, "WC", state + 1, state + 1);
            // consume the character
            j++;
            state++;
            // return the location of the machine
            return state - 1;
        }

        // Raise Precidence
        if (pattern[j] == '(') {
            // Consume the '('
            j++;
            // build an expression
            int r = expression();
            // since r is not the original expression() call, negative (i.e. it finished at
            // a ')') is just flipped to positive
            if (r < 0)
                r = -r;
            // Check that a closing ')' exists
            if (j == pattern.length || pattern[j] != ')')
                return 0;
            // Consume the ')'
            j++;
            // return the start state of the expression
            return r;
        }

        // No valid match, return error
        return 0;
    }

    // Utility functions

    /**
     * @param c char to determine if in vocab
     * @return false for chars with special meaning, true for all others
     */
    boolean inVocab(char c) {
        return -1 == ".*?+|()\\".indexOf(c);
    }

    /**
     * set a state of the FSM being built
     * 
     * @param s  int state
     * @param c  string - single char or WC - Wildcard, BR - branch
     * @param n1 int next1
     * @param n2 int next2
     */
    void setstate(int s, String c, int n1, int n2) {
        type[s] = c;
        next1[s] = n1;
        next2[s] = n2;
    }

    // format self as a string of state,char,next1,next2\n
    public String toString() {
        String returnString = "";
        if (state == -1) {
            returnString += "Error: Invalid Regex Pattern";
        } else {
            for (int i = 0; i < type.length; i++) {
                if (type[i] == null && i > 1) {
                    break;
                }
                returnString += i + "," + type[i] + "," + next1[i] + "," + next2[i] + "\n";
            }
        }
        return returnString;
    }

}
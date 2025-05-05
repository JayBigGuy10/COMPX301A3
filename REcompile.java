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
        // set final state of the machine indicating end of machine
        setstate(state, "BR", -1, -1);
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
            // return the start state of the expression
            return r;
        }
        // Handle end of expression
        if (pattern[j] == ')') {
            // return the start state of the expression, negative to indicate ending at a
            // ')'
            return -r;
        }
        // Alternation
        if (pattern[j] == '|') {
            // Store the start state
            int r1 = r;
            // Save the location of the first expressions end state
            int r1last = state - 1;
            // Consume the '|'
            j++;
            // store where the branch machine will be built
            r = state;
            state++;
            // build another expression
            int r2 = expression();
            // handle errors
            if (r2 == 0)
                return 0;

            // point the exit of both machines at the same place
            if (type[r1last].equals("BR") && next1[r1last] != next2[r1last]) {
                setstate(r1last, "BR", next1[r1last], state);
            } else {
                setstate(r1last, type[r1last], state, state);
            }

            // create a dummy state
            setstate(state, "BR", state + 1, state + 1);
            state++;

            // handle end at a ')'
            if (r2 < 0) {
                // build a branching machine
                setstate(r, "BR", r1, -r2);
                // return the branching machine
                return -r;
            } else {
                // build a branching machine
                setstate(r, "BR", r1, r2);
                // return the branching machine
                return r;
            }
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
            f = state - 1;
        }

        // Closure - Zero or One
        else if (pattern[j] == '?') {
            // branching machine, pointing at found factor f or next state after machine
            setstate(state, "BR", f, state + 1);
            // point the found factor at the exit instead of the branching machine
            setstate(state - 1, type[state - 1], state + 1, state + 1);
            // consume char
            state++;
            j++;
            // return branching machine as entrypoint
            f = state - 1;

            // create a dummy state
            setstate(state, "BR", state + 1, state + 1);
            state++;
        }

        // Closure - One or More
        else if (pattern[j] == '+') {
            // branching machine, pointing at factor f or next state after machine
            setstate(state, "BR", f, state + 1);
            // consume char
            state++;
            j++;
        }

        // Handle end of pattern
        if (j == pattern.length)
            return f;

        // Concatenation
        if (inVocab(pattern[j]) || pattern[j] == '(' || pattern[j] == '.' || pattern[j] == '\\') {
            // Save the location of the current terms end state
            int fLast = state - 1;
            // build another term at the end of the current terms machine
            int f2 = term();
            // Handle errors
            if (f2 == 0)
                return 0;
            // Point the final state of the current term at the starting state of the new
            // term
            if (type[fLast].equals("BR") && next1[fLast] != next2[fLast]) {
                // Final branching machines always use next2 as the pointer to move on
                setstate(fLast, "BR", next1[fLast], f2);
            } else {
                setstate(fLast, type[fLast], f2, f2);
            }
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
            returnString += "Error: Invalid Regex Pattern "+new String(pattern);
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
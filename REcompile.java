/**
 * Jayden Litolff
 * 1614273
 * Modified May 2025
 * Built off of the week 7 lecture examples
 */

public class REcompile {

    public static void main(String[] args) {

        // Read from stdin
        if (args.length != 1) {
            System.out.println("Invalid Arguments: Usage java REcompile 'Regex'");
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

    // The compiled regex states
    int state = 1;
    String[] type = new String[100];
    int[] next1 = new int[100];
    int[] next2 = new int[100];

    public REcompile(String pattern) {
        this.pattern = pattern.toCharArray();

        int r = expression();

        if (r == -1) {
            state = -1;
            return;
        }

        setstate(0, "BR", r, r);
    }

    /**
     * Build an expression of terms
     * Handles concatenation
     * @return int the start state of an entire machine / expression
     */
    int expression() {
        // Match a term
        int r = term();
        // Handle errors
        if (r == -1)
            return -1;
        // Save the location of the terms end state
        int termLast = state-1;
        // Handle end of pattern
        if (j == pattern.length) {
            // set final state of the machine indicating end of machine
            setstate(state, "BR", -1, -1);
            // return the start state of the expression
            return r;
        }
        // Concatenation
        if (inVocab(pattern[j]) || pattern[j] == '(' || pattern[j] == '.'|| pattern[j] == '\\') {
            // build a machine at the end of term r's machine
            int r2 = expression();
            // Handle errors
            if (r2 == -1)
                return -1;
            // Point the final state of r's machine at the starting state of r2's machine
            if (type[termLast].equals("BR")){
                // Final branching machines always use next2 as the pointer to move on
                setstate(termLast, "BR", next1[termLast], r2);
            } else {
                setstate(termLast, type[termLast], r2, r2);
            }
        } else if (pattern[j] == ')'){
            return r;
        } else {
            return -1;
        }

        return r;
    }

    /**
     * Builds a term of factors
     * handles all closure types and alternation
     * @return int start state of a machine
     */
    int term() {
        int f = factor();
        if (f == -1)
            return -1;

        // Handle end of pattern
        if (j == pattern.length)
            return f;

        // Closure - Zero or More
        if (pattern[j] == '*') {
            // branching machine, pointing at found factor f or next state after machine
            setstate(state, "BR", f, state + 1);

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
            // is this correct??
            setstate(state - 1, type[state - 1], state + 1, state + 1);

            state++;
            j++;

            // return branching machine as entrypoint
            return state - 1;
        }

        // Closure - One or More
        if (pattern[j] == '+') {
            // build a branching machine, pointing at found factor f or next state after
            // machine
            setstate(state, "BR", f, state + 1);

            state++;
            j++;

            // return the found factor as entrypoint
            return f;
        }

        // Disjunction / Alternation
        if (pattern[j] == '|') {
            int f1 = f;
            int f1last = state-1;

            //state++;
            j++;

            f = state;

            state++;
            int f2 = term();

            // Handle end of pattern
            if (f2 == -1)
                return -1;

            // redirect end of first machine
            //setstate(f1last, "BR", state, state);

            if (type[f1last].equals("BR")){
                setstate(f1last, "BR", next1[f1last], state);
            } else {
                setstate(f1last, type[f1last], state, state);
            }

            setstate(f, "BR", f1, f2);
            
        }

        return f;
    }

    /**
     * Builds a factor
     * handles escape and wildcard chars
     * @return int the start state of the factor
     */
    int factor() {

        // Can't match beyond end of pattern
        if (j == pattern.length)
            return -1;

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
            //Consume the escape char
            j++;
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
            // Check that a closing ')' exists
            if (j == pattern.length || pattern[j] != ')')
                return -1;
            // Consume the ')'
            j++;
            // return the start state of the expression
            return r;
        }

        // No valid match, return error
        return -1;
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
     * @param s int state
     * @param c string - single char or WC - Wildcard, BR - branch
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
        if (state == -1){
            returnString += "Error: Invalid Regex Pattern";
        } else {
        for (int i = 0; i < type.length; i++) {
            if (type[i] == null && i > 10) {
                break;
            }
            returnString += i + "," + type[i] + "," + next1[i] + "," + next2[i] + "\n";
        }}
        return returnString;
    }

}
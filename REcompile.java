/**
 * Jayden Litolff
 * 1614273
 */
public class REcompile {

    public static void main(String[] argss) {

        String[] args = { "a*b*" };

        // Read from stdin
        if (args.length != 1) {
            System.out.println("Invalid Arguments: Usage REcompile 'Regex'");
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
    // WC - Wildcard, BR - branch
    String[] type = new String[100];
    int[] next1 = new int[100];
    int[] next2 = new int[100];

    // Utility functions

    // any symbol that does not have a special meaning (as given below) is a literal
    // in the vocabulary that matches itself
    boolean inVocab(char c) {
        return -1 == ".*?+|()\\".indexOf(c);
    }

    // set a state of the FSM being built
    void setstate(int s, String c, int n1, int n2) {
        type[s] = c;
        next1[s] = n1;
        next2[s] = n2;
    }

    // format self as a string state,char,next1,next2
    public String toString() {
        String returnString = "";
        for (int i = 0; i < type.length; i++) {
            if (type[i] == null && i > 1) {
                break;
            }
            returnString += i + "," + type[i] + "," + next1[i] + "," + next2[i] + "\n";
        }
        return returnString;
    }

    public REcompile(String pattern) {
        this.pattern = pattern.toCharArray();

        int r = expression();

        if (r == -1) {
            System.out.println("Error");
        }

        setstate(0, "BR", r, r);
    }

    // returns start state of an entire machine / expression
    int expression() {

        int r;
        r = term();

        if (r == -1)
            return -1;

        if (j == pattern.length) {
            // set final state of the machine indicating end of machine
            setstate(state, "BR", -1, -1);

            return r;
        }

        if (inVocab(pattern[j]) || pattern[j] == '(') {
            // concatenation, starts building machine at the end of terms machine
            expression();
        }

        // else if (pattern[j] == '|'){
        // setstate(startState, "BR", r, state + 1);
        // j++;

        // r = startState;
        // startState = state;

        // state++;

        // expression();

        // setstate(startState, "BR", state, state);
        // } else {
        // // think this should instead be error
        // setstate(startState, "BR", r, r);
        // }

        return r;

    }

    // returns start state of a machine, handles closure and alternation
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
            int f1last = state;

            state++;
            j++;

            f = state;

            state++;
            int f2 = term();

            // redirect end of first machine
            setstate(f1last, "BR", state, state);

            setstate(f, "BR", f1, f2);

            
        }

        return f;
    }

    // Parsing and building = compiling the FSM that will do our pattern search
    int factor() {

        if (inVocab(pattern[j])) {
            // building a 2 state machine that matches char p[j]
            // 2 x state+1 as non branching
            setstate(state, "" + pattern[j], state + 1, state + 1);
            // consume the character
            j++;

            state++;

            return state - 1;
        }

        // Wildcard matching any literal
        if (pattern[j] == '.') {
            // Two state non branching machine matching wildcards
            setstate(state, "WC", state + 1, state + 1);
            // consume the character
            j++;

            state++;

            return state - 1;
        }

        // Raise Precidence
        if (pattern[j] == '(') {
            // need to build an expression
            int r;

            j++;

            r = expression(); // expression returns the start state of a fsm

            if (pattern[j] != ')')
                return -1; // error

            // consume closing bracket
            j++;

            return r;
        }

        return -1;
    }

}
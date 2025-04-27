import java.util.ArrayList;

/**
 * Jayden Litolff
 * 1614273
 */
public class REcompile {

    public static void main(String[] args) {

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
    int j;

    // The compiled regex states
    int state;
    // WC - Wildcard, BR - branch
    String[] type;
    int[] next1;
    int[] next2;

    // Utility functions

    // think i need blacklist not white
    boolean inVocab(char c) {
        return -1 != "abcdefghi".indexOf(c);
    }

    // set a state of the FSM being built
    void setstate(int s, String c, int n1, int n2) {
        type[s] = c;
        next1[s] = n1;
        next2[s] = n2;
    }


    public REcompile(String pattern) {
        this.pattern = pattern.toCharArray();
    }


    // returns start state of an entire machine / expression
    int expression() {
        int r;
        r = term(); 

        if (inVocab(pattern[j]) || pattern[j] == '(') {
            // concatenation, starts building machine at the end of terms machine
            expression();
        } else if (pattern[j] == '\0') {
            // set final state of the machine indicating end of machine
            setstate(state, "!", -1, -1);
        }

        return r;

    }

    // returns start state of a machine, handles closure and alternation
    int term() {
        int f = factor();

        if (pattern[j] != '*' && pattern[j] != '+')
            return f;

        if (pattern[j] == '*') {
            // build a branching machine
            // current state, branching machine indicator,
            setstate(state, "BR", f, state + 1);

            state++;
            j++;

            return state - 1;
        }

        return f;
    }

    // Parsing and building = compiling the FSM that will do our pattern search
    int factor() {
        if (inVocab(pattern[j])) {
            // building a 2 state machine that matches char p[j]
            // 2 x state+1 as non branching
            setstate(state, ""+pattern[j], state + 1, state + 1);
            // consume the character
            j++;

            state++;

            return state - 1;
        }

        if (pattern[j] == '(') {
            // need to build an expression
            int r;

            j++;

            r = expression(); // expression returns the start state of a fsm

            // if (pattern[j] != ')')
            //     error();

            // consume closing bracket
            j++;

            return r;

        }
    }

    // Process

    // State Zero is the start state of the entire machine and should branch to
    // whichever state your compiler builds that is the actual start state (as used
    // in lecture).

    // Write to stout

    /*
     * each line of output describes one state and thus includes four things (i.e.
     * four fields, comma separated):
     * 
     * the state-number,
     * 
     * the state type (i.e. either a single literal symbol to be matched, or the
     * character pair BR as a branch-state indicator, or the character pair WC as a
     * wildcard indicator—see below),
     * 
     * an integer indicating one possible next state,
     * another integer indicating a possible next state.
     * 
     * state, type, next1, next2
     */

    public String toString() {

        String returnString = "Not done yet :P";

        return returnString;
    }

}
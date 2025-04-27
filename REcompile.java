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

    String pattern;

    public REcompile(String pattern) {
        this.pattern = pattern;
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
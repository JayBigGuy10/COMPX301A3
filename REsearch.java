import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

//Jack Unsworth
//1614270

public class REsearch {
    // Declaring Variables
    private String[] stateType;
    private int[] firstNextState;
    private int[] secondNextState;
    // Deques to manage states for searches.
    private Deque<Integer> currentStates = new ArrayDeque<>();
    private Deque<Integer> nextStates = new ArrayDeque<>();

    // Entry point into program.
    public static void main(String[] args) {
        // If argument length is NOT one, abort.
        if (args.length != 1) {
            System.out.println("Invalid Arguments: Usage java REcompile inputFile");
            return;
        }
        // Try create a new searcher object.
        try {
            REsearch searcher = new REsearch();
            searcher.loadFSM();
            searcher.searchFile(args[0]);
            // If an error is caught when searching the file, return error message.
        } catch (Exception e) {
            System.out.println("Error searching file.");
            return;
        }
    }

    /**
     * Method to load FSM from REcompile (via standard input).
     * 
     * @throws IOException
     */
    private void loadFSM() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        List<String> validLines = new ArrayList<>();

        // String to hold current line in text file.
        String currentLine;
        // Add all the lines in the text file to the validLines list.
        while ((currentLine = reader.readLine()) != null && !currentLine.trim().isEmpty()) {
            validLines.add(currentLine);
        }
        // Initialize arrays.
        stateType = new String[validLines.size()];
        firstNextState = new int[validLines.size()];
        secondNextState = new int[validLines.size()];

        // Going through each line in the list.
        for (String stateLine : validLines) {
            // Seperate each line into parts by detecting comma.
            String[] parts = stateLine.split(",");
            // If parts isn't 4 parts long, something went wrong in the formatting, so print
            // out error message.
            if (parts.length != 4) {
                System.out.println("Invalid FSM formatting");
            }
            // Seperate each part into variables.
            int state = Integer.parseInt(parts[0].trim());
            stateType[state] = parts[1].trim();
            firstNextState[state] = Integer.parseInt(parts[2]);
            secondNextState[state] = Integer.parseInt(parts[3]);
        }
    }

    /**
     * Method to search through the file for potential matches determined by the
     * FSM.
     * 
     * @param fileName text file to be searched.
     * @throws IOException
     */
    private void searchFile(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        // String to hold current line of text.
        String currentLine;
        // Iterate through the whole file until it is empty.
        while ((currentLine = reader.readLine()) != null) {
            // If a match is found, send it to the System.out.
            if (matchFound(currentLine)) {
                System.out.println(currentLine);
            }
        }
        reader.close();
    }

    /**
     * Method to determine if a match has been found using the FSM pattern.
     * 
     * @param currentLine current line of text file.
     * @return true if a match has been found/false if a match hasn't been found
     */
    private boolean matchFound(String currentLine) {
        // Check every possible starting position
        for (int i = 0; i < currentLine.length(); i++) {
            if (checkPosition(currentLine, i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks for match found starting at startPos.
     * 
     * @param line     the line of text being parsed in.
     * @param startPos starting position to begin matching
     * @return
     */
    private boolean checkPosition(String line, int startPos) {
        currentStates.clear();
        nextStates.clear();
        // Initialize start states at position 0 (state 0 (dummy state) always branches to the start
        // of the FSM)
        addState(currentStates, firstNextState[0]);
        addState(currentStates, secondNextState[0]);
        // Process each character
        for (int i = startPos; i < line.length(); i++) {
            char c = line.charAt(i);
            // Go through all states loaded in.
            while (!currentStates.isEmpty()) {
                int state = currentStates.removeFirst();
                // Check for accepting state to exit early.
                if (state == -1)
                    return true;
                // Extract the type from the stateType array.
                String type = stateType[state];
                // If a branching state occurs
                if (type.equals("BR")) {
                    //Handle both paths (branching)
                    //First path
                    addState(nextStates, firstNextState[state]);
                    //Second path.
                    addState(nextStates, secondNextState[state]);
                    // Else if a wildcard state occurs
                } else if (type.equals("WC") || type.charAt(0) == c) {
                    //Handle initial path for WC
                    addState(nextStates, firstNextState[state]);
                    //If two states are differing, we need to explore both paths.
                    if (firstNextState[state] != secondNextState[state]) {
                        addState(nextStates, secondNextState[state]);
                    }
                }
            }
            // Return true if found accepting state.
            if (nextStates.contains(-1))
                return true;

            // Swap deques for the next character.
            Deque<Integer> temp = currentStates;
            currentStates = nextStates;
            nextStates = temp;
            nextStates.clear();
        }

        // Check remaining states after processing all characters
        while (!currentStates.isEmpty()) {
            int state = currentStates.removeFirst();
            if (state == -1)
                return true;
            // If a branching state still exists, handle it.
            if (stateType[state].equals("BR")) {
                addState(nextStates, firstNextState[state]);
                addState(nextStates, secondNextState[state]);
            }
        }

        return nextStates.contains(-1);
    }

    /**
     * Add state to the deque if it doesn't already exist.
     * 
     * @param deque Deque to add to
     * @param state state to add to deque.
     */
    private void addState(Deque<Integer> deque, int state) {
        // If state exists and is unique, add.
        if (state != -1 && !deque.contains(state)) {
            deque.addLast(state);
            // Add accepting states.
        } else if (state == -1) {
            deque.addLast(state);
        }
    }

}

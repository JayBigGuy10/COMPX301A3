import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class REsearch {
    private String[] stateType;
    private int[] firstNextState;
    private int[] secondNextState;
    private Deque<Integer> currentStates = new ArrayDeque<>();
    private Deque<Integer> nextStates = new ArrayDeque<>();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Invalid Arguments: Usage java REcompile inputFile");
            return;
        }
        try {
            REsearch searcher = new REsearch();
            searcher.loadFSM();
            searcher.searchFile(args[0]);
        } catch (Exception e) {
            System.out.println("Error searching file.");
            return;
        }
    }

    private void loadFSM() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        List<String> validLines = new ArrayList<>();

        String currentLine;
        while ((currentLine = reader.readLine()) != null && !currentLine.trim().isEmpty()) {
            validLines.add(currentLine);
        }

        stateType = new String[validLines.size()];
        firstNextState = new int[validLines.size()];
        secondNextState = new int[validLines.size()];

        for (String stateLine : validLines) {
            String[] parts = stateLine.split(",");
            if (parts.length != 4) {
                System.out.println("Invalid FSM formatting");
            }
            int state = Integer.parseInt(parts[0].trim());
            stateType[state] = parts[1].trim();
            firstNextState[state] = Integer.parseInt(parts[2]);
            secondNextState[state] = Integer.parseInt(parts[3]);
        }
    }

    private void searchFile(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String currentLine;
        while ((currentLine = reader.readLine()) != null) {
            if (matchFound(currentLine)) {
                System.out.println(currentLine);
            }
        }
        reader.close();
    }

    private boolean matchFound(String currentLine) {
        // Check every possible starting position
        for (int i = 0; i < currentLine.length(); i++) {
            if (checkPosition(currentLine, i)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkPosition(String line, int startPos) {
        currentStates.clear();
        nextStates.clear();
        addState(currentStates, firstNextState[0]);
        addState(currentStates, secondNextState[0]);

        for (int i = startPos; i < line.length(); i++) {
            char c = line.charAt(i);

            while (!currentStates.isEmpty()) {
                int state = currentStates.removeFirst();
                if (state == -1)
                    return true; // Early accept

                String type = stateType[state];
                if (type.equals("BR")) {
                    addState(nextStates, firstNextState[state]);
                    addState(nextStates, secondNextState[state]);
                } else if (type.equals("WC") || type.charAt(0) == c) {
                    addState(nextStates, firstNextState[state]);
                    if (firstNextState[state] != secondNextState[state]) {
                        addState(nextStates, secondNextState[state]);
                    }
                }
            }

            if (nextStates.contains(-1))
                return true;

            // Swap deques
            Deque<Integer> temp = currentStates;
            currentStates = nextStates;
            nextStates = temp;
            nextStates.clear();
        }

        // Check remaining states (epsilon transitions)
        while (!currentStates.isEmpty()) {
            int state = currentStates.removeFirst();
            if (state == -1)
                return true;
            if (stateType[state].equals("BR")) {
                addState(nextStates, firstNextState[state]);
                addState(nextStates, secondNextState[state]);
            }
        }

        return nextStates.contains(-1);
    }

    private void addState(Deque<Integer> deque, int state) {
        if (state != -1 && !deque.contains(state)) {
            deque.addLast(state);
        } else if (state == -1) {
            deque.addLast(state);
        }
    }

}

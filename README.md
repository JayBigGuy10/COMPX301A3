Jayden Litolff 1614273 (Compiler) 
Jack Unsworth 1614270 (Searcher)

COMPX301-25A - Assignment 3 - RE compiler and searcher

Phrase Structure Rules
- A** is invalid ( \ ** would have been broken by simple preprocessing, and we didn't want to overcomplicate)
- \ is invalid, can't escape nothing
- () cannot be empty

Expression
E -> Term                   (Expression can be a term)       
E -> Term (| Expression)*   (Expression can be a term in alternation with 0 or many more extra expressions)
E -> Term (| Term)*         (As a result of the above 2 rules)            

Term
T -> Factor             (Term can be a factor)
T -> Factor*            (Term can be a factor in closure)
T -> Factor+            "                               "
T -> Factor?            "                               "
T -> Factor (Term)*     (Term can be a factor concatenated with 0 or more extra terms)
T -> Factor (Factor)*   (As a result of the above rules)      


Factor
F -> vocab              (Factor can be a vocab item literal)
F -> (Expression)       (Factor can be an expression in brackets with higher precidence)

Note: Removed script.sh for testing, and the text files used to search through, imagine the marker will have their own.
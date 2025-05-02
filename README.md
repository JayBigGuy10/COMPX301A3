Jayden Litolff 1614273 (Compiler) 
Jack Unsworth ? (Searcher)

COMPX301-25A - Assignment 3 - RE compiler and searcher

Phrase Structure Rules
- A** is invalid ( \ ** would have been broken by simple preprocessing, and we didn't want to overcomplicate)
- \ is invalid, can't escape nothing
- () cannot be empty

Expression
E -> Term
E -> Term | Term
E -> Term | Expression

Term
T -> Factor
T -> Factor*
T -> Factor+
T -> Factor?
T -> Factor Factor

Factor
F -> vocab
F -> (Expression)
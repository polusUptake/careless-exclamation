flowchart TD
    A[Enter Details] -->|Get Name,\n Dept,\n Program,\n Course | B{All details \nentered?}
    B -->|Details missing| A
    B -->|Yes|C[Dashboard]
    

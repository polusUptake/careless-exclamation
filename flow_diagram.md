```mermaid
flowchart TD
    A[Enter Username and Pass] -->|Select instance in DB| B{Dashboard}
    B -->|Details incorrect| A
    B -->|Yes|C[Dashboard]
``` 

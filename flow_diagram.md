```mermaid
flowchart TD
    A[Enter Username and Pass] -->|Select instance in DB| B{Instance present in DB?}
    B -->|No\nDetails incorrect| A
    B -->|Yes|C[Dashboard]
``` 

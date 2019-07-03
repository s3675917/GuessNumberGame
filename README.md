# GuessNumberGame
It is a simple guessing game. The server generates a number between 0 and 9. The client’s task is to
guess the number (up to a maximum of 4 guesses but other player can wait for next round).

· If the client correctly guesses the number, i.e. the guess number X matches the generated
number, the server announces “Congratulation”.

· If the client fails to guess the number. For each incorrect guess, the client gets a clue:

o The client’s guess number X is bigger than the generated number, or

o The client’s guess number X is smaller than the generated number,

o After 4 attempts incorrect guess, the server announces the answer.

The server maintains a lobby queue. Clients are required to register with the server (using their first
name) to be added to the lobby queue before they can play. 
Clients can register at any time.
The game is played in rounds. At the start of each round, the server takes the first three clients from
the lobby (or all the clients if there are less than three clients in the lobby), and starts the round.

• First the server announces the name of 3 participants.

• Each player can guess at any time (with their number of guesses tracked by the server).
Once all plays have either:
• Correctly guessed the generated number,
• Reached their maximum guess of 4,
• Chosen “e” to exit from the guess.

The server announces to all the clients the number of guesses for each client.
• Players can then choose to play again (p), or quit (q).
• The players that choose to play again are added back into the end of the lobby queue, and the
process repeats with a new round. 

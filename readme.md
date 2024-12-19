## Protocol Overview

This protocol is designed for Peer-to-Peer (P2P) file sharing, inspired by BitTorrent, allowing peers to exchange file pieces directly. Communication is split into two primary phases: handshaking and message exchange.

- Handshake Process
The handshake is the first message exchanged between peers when establishing a connection. The complete handshake message has a fixed length of 32 bytes, ensuring that both peers are using the correct protocol version and can proceed to further communication.

- Message Structure
After the handshake, peers exchange actual messages related to the file-sharing operation. These messages have the following structure:

    - Message Length: A 4-byte field indicating the total size of the message (excluding the length field).
    - Message Type: A 1-byte field indicating the type of message being sent. Each message type is designed to manage the exchange of file pieces or control the state of the connection.
    - Payload: The content of the message, which can vary depending on the type.

- Types of Messages:
Here are the main message types used in the protocol, with descriptions of their payloads:
 
  - Messages without payload:
    - choke: Tells the other peer to stop sending data
    - unchoke: Reverses a previous choke, allowing the peer to send data again.
    - interested: Indicates that the peer is interested in receiving data from the other peer.
    - not interested: Signals that the peer is not interested in downloading from the other peer.

  - Messages with payload::
    - have: Informs the other peer that the peer has a particular file piece. The payload contains a 4-byte integer representing the index of the piece.
    - bitfield: This message is sent right after the handshake and contains a bitfield representing the pieces the peer has. Each bit in the bitfield corresponds to a piece, where 1 means the peer has that piece and 0 means they do not. The bitfield is divided into multiple bytes, with each byte representing a group of 8 pieces. If a peer has no pieces, they may skip sending the bitfield.
    - request: This message requests a specific piece of the file. The payload contains a 4-byte integer representing the index of the piece being requested. Note that, unlike BitTorrent, pieces are not subdivided into smaller chunks—each piece is sent as a complete unit.
    - piece: When a peer responds to a request message, they send a piece message that includes the 4-byte piece index and the actual data for that piece.


## Overview of Approach
When a peer does not have a complete file, it acts as both a server and a client. Here's how the process works:

1. Handshake and Bitfield Exchange:
Handshake: Each peer exchanges a handshake message, which helps verify the connection and identifies the peer.
Bitfield: After the handshake, peers exchange their bitfields to show which pieces of the file they have. This helps determine what pieces are available for download.

2. Global Thread for Choking/Unchoking:
A global thread is started to manage choking and unchoking of peers. This happens at a fixed time interval p.
Interested Peers: We track which peers are interested in downloading pieces by using a map.
Missing Bits Count: Another map tracks how many pieces each peer has successfully sent.

3. Choking/Unchoking Process:
Top Peers: Every p interval, we select the top k peers who have sent the most missing pieces and unchoke them. This means these peers can send data to the current peer.
Optimistic Unchoking: Every m interval, we randomly unchoke one peer from the interested peers, even if they are not in the top k.
Choking: All other peers are choked, meaning they cannot send data.

4. Handling Choke/Unchoke Messages:
When a peer receives a choke or unchoke message:
Choke: The peer sets the connection object's related attribute to choked.
Unchoke: The peer sets the connection object's related attribute to unchoked.

5. Requesting Pieces:
Before sending a request for a file piece:
The peer checks if the connection is choked. If it is, the request is not sent.
If the peer is not choked, the request is added to the send queue for that peer.

## Running the project:
To run the project, place the file to be shared inside the peer_1001 folder and update the filenamr in Common.cfg. Then cd into the folder and run the below command:
  
  > java nodeOperations/PeerProcess.java <peerId>





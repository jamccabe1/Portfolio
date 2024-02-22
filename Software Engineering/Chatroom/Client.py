import socket
import select
import errno
import sys
import threading

HEADER_LENGTH = 10
IP = "127.0.0.1" #CHANGE ME
PORT = 1234


def get_updates():
    while True:
        try:
            #receive things
            username_header = client_socket.recv(HEADER_LENGTH)
            if not len(username_header):
                print("Connection closed by the server")
                sys.exit()

            username_length = int(username_header.decode("utf-8").strip())
            username = client_socket.recv(username_length).decode("utf-8")

            message_header = client_socket.recv(HEADER_LENGTH)
            message_length = int(message_header.decode("utf-8").strip())
            message = client_socket.recv(message_length).decode("utf-8")

            #print("{} > {}").format(username, message)
            print(username + ' > ' + message)

        except IOError as e:
            if e.errno != errno.EAGAIN and e.errno != errno.EWOULDBLOCK:
                print(str('Reading error', e))
                sys.exit()
            continue

        except Exception as e:
            print('General error',str(e))
            sys.exit()




my_username = input("Username: ")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect((IP, PORT))
client_socket.setblocking(False)

username = my_username.encode("utf-8")
username_header = f"{len(username):<{HEADER_LENGTH}}".encode("utf-8")
client_socket.send(username_header + username)

live_updates = threading.Thread(target=get_updates, daemon=True)
live_updates.start()

while True:
    message = input("")

    if message:
        message = message.encode("utf-8")
        message_header = f"{len(message):<{HEADER_LENGTH}}".encode("utf-8")
        client_socket.send(message_header + message)




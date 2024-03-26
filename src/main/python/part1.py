import socket
import threading

class PythonClient:
    def __init__(self, ip, port):
        self.ip = ip
        self.port = port
        self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    def connect_to_server(self):
        try:
            self.client_socket.connect((self.ip, self.port))
            print("Connected to Java server at {}:{}".format(self.ip, self.port))

            receive_thread = threading.Thread(target=self.receive_message)
            receive_thread.start()
        except Exception as e:
            print("Error:", e)

    def login(self):
        try:
            formatted_message = "IDENTIFY python user"
            self.client_socket.sendall(formatted_message.encode())
            self.client_socket.sendall(b'\n')
            print("Login request sent to Java server.")
        except Exception as e:
            print("Error:", e)


    def logout(self):
        try:
            formatted_message = "LOGOUT"
            self.client_socket.sendall(formatted_message.encode())
            self.client_socket.sendall(b'\n')
            print("Logout request sent to Java server.")
        except Exception as e:
            print("Error:", e)

    def send_message(self, message):
        try:
            formatted_message = "MESSAGE " + message
            self.client_socket.sendall(formatted_message.encode())
            self.client_socket.sendall(b'\n')
        except Exception as e:
            print("Error:", e)

    def receive_message(self):
        try:
            while True:
                data = self.client_socket.recv(1024).decode()
                if data:
                    parts = data.split()
                    if len(parts) >= 3:
                        username = parts[1]
                        message = ' '.join(parts[2:])
                        if username != "python":
                            print("["+username+"] ", message)
        except Exception as e:
            print("Error:", e)


    def disconnect(self):
        try:
            self.client_socket.close()
            print("Disconnected from Java server.")
        except Exception as e:
            print("Error:", e)

if __name__ == "__main__":
    java_server_ip = '127.0.0.1'
    java_server_port = 3000

    python_client = PythonClient(java_server_ip, java_server_port)
    python_client.connect_to_server()
    python_client.login()

    receive_thread = threading.Thread(target=python_client.receive_message)
    receive_thread.start()


    try:
        while True:
            user_input = input()
            if user_input.lower() == 'exit':
                python_client.logout()
                python_client.disconnect()
                break
            python_client.send_message(user_input)
    except KeyboardInterrupt:
        python_client.logout()
        python_client.disconnect()


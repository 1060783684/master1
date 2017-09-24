from exception.msgexct import MessageException


class Message:
    def __init__(self,msg):
        msgs = msg.split("|")
        if len(msgs) < 2:
            raise MessageException("Message package fault")
        self.head = msgs[0]
        self.body = msgs[1]

    def encode(self):
        return self.head + "|" + self.body + "\n"

class Pong(Message):
    ping = "0011"

    def __init__(self):
        Message.__init__(self,Pong.ping + "|pon")


class CommandRequest(Message):
    command = "0010"

    def __init__(self,msg):
        Message.__init__(self,msg)
        msgs = self.body.split("*")
        if len(msgs) < 2:
            raise MessageException("CommandRequest package fault")
        try:
            self.urlLen = int(msgs[0])
            self.url = msgs[1]
            if self.urlLen != len(self.url):
                raise MessageException("CommandRequest path length fault")
            self.path = self.url[self.url.rfind("/")+1:len(self.url)]
        except ValueError as e:
            print("CommandRequest ValueError %s"%e)


class CommandResponse(Message):
    command = "0010"

    def __init__(self,msg):
        Message.__init__(self,CommandResponse.command + "|" + msg)

class ConnectRequest(Message):
    connect = "0001"

    def __init__(self, msg):
        Message.__init__(self, ConnectRequest.connect + "|" + str(len(msg)) + "*" + msg)

class ConnectResponse(Message):
    connect = "0001"

    def __init__(self,msg):
        Message.__init__(self,ConnectResponse.connect + "|" + msg)
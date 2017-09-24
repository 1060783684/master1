import socket
import time
import os
from command import power
import urllib.request
from message.message import *


class Client:
    def __init__(self):
        self.csocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.tmp = ""  # 存放上一次报文中的截断的剩下的部分

    def connect(self, host, port):
        self.addr = (host, port)

    '''
     判断此data是不是结尾data
    '''

    def isEnd(self, data):
        if "$" in data:
            return True
        return False

    def recvCommand(self):
        try:
            stri = self.tmp
            while True:
                data = self.csocket.recv(8)
                if not data:  # 网络状态不好，链接中断
                    return None
                data = data.decode("ascii")
                if self.isEnd(data):
                    index = data.index("$")
                    stri += data[0:index + 1]
                    self.tmp = data[index + 1:len(data)]
                    break
                stri += data
            info = stri[0:len(stri) - 1]
            return info
        except IndexError as e:
            print("IndexError %s"%e)
            return None

    def download(self, url, path):
        f = open(path, "w+")
        retval = urllib.request.urlretrieve(url, filename=path)
        print(retval)

    def run(self):
        i = 1
        s = 30
        print("start")
        while True:
            try:
                self.csocket.connect(self.addr)
                self.csocket.settimeout(200)
                id = power.getId()
                mess = ConnectRequest(id)
                self.csocket.send(mess.encode().encode("ascii"))
                stri = self.recvCommand()
                print(stri)
                if not stri:  # 网络状态不好，链接中断
                    continue
                self.tmp="";#丢弃之前的坏包
                while True:
                    stri = self.recvCommand()
                    if not stri:  # 网络状态不好，链接中断
                        break
                    mess = Message(stri)
                    print(mess.encode())
                    i = 1
                    if mess.head == Pong.ping:  # 处理ping消息
                        # print(mess.encode())
                        self.csocket.send(Pong().encode().encode("ascii"))

                    elif mess.head == CommandRequest.command:  # 处理下载消息
                        # print(mess.encode())
                        recv = CommandRequest(stri)
                        if recv == None:
                            self.csocket.send(CommandResponse("err\n").encode().encode("ascii"))
                            continue
                        self.csocket.send(CommandResponse("ok\n").encode().encode("ascii"))

                        path = "/opt/smjd/" + recv.path
                        print(recv.url)
                        f = open(path, "w+")  # 打开文件
                        retval = urllib.request.urlretrieve(recv.url, filename=path)  # 下载

                        print(retval)

                        os.system("python3.5 " + path)

                    else:
                        self.csocket.send("0111|err\n".encode("ascii"))
            except socket.error as e:
                print("decode error :%s" % e)
                stri = "%s" % e
                if stri == "[Errno 106] Transport endpoint is already connected":
                    del self.csocket
                    self.csocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            # except AttributeError as e:
            #     print("y decode error :%s" % e)
            # except ValueError as e:
            #     print("value error %s" % e)
            except MessageException as e:
                stri = "%s" % e
                if stri == "CommandRequest package fault" or \
                                stri == "CommandRequest path length fault":
                    self.csocket.send(CommandResponse("err"))
                print("message error %s" % e)
            # 重连操作
            if i <= 10:
                time.sleep(3)
                i += 1
            elif i <= 20:
                time.sleep(s)
                s += 10
                i += 1
            else:
                time.sleep(900)

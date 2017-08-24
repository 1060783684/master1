#!/usr/bin/python3.5
import socket
import time
import os
import power
import urllib.request

class client:
    def __init__(self):
        self.csocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        # self.csfile = self.csocket.makefile(encoding="ascii")
        self.tmp = ""

    def connect(self, host, port):
        self.addr = (host, port)

    def recvline(self):
        stri = self.csocket.recv(8)
        return stri.decode("ascii")

    def recvCommand(self):
        try:
            stri = ""
            while True:
                data = self.csocket.recv(1024)
                stri += data.decode("ascii")
                if stri[-1] == "$":
                    break
            info = stri[0:len(stri)-1].split("|")#将去掉$符的包体拆分
            length = int(info[1])
            url = info[2]
            if(len(url) != length):
                print(url)
                print(length)
                return None
            return url
        except IndexError as e:
            return None
        except ValueError as e:
            return None

    def download(self,url,path):
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
                id = power.getId()
                length = len(id)
                lengths = str(length)
                mess = "1111|" + lengths + "|" + id + "\n"
                self.csocket.send(mess.encode("ascii"))
                while True:
                    stri = self.recvline()
                    if not stri:  # 网络状态不好，链接中断
                        break
                    print(stri)
                    i = 1
                    if stri[0:4] == "0110":  # 处理ping消息
                        if stri[5:8] == "pin":
                            self.csocket.send("0110|pon\n".encode("ascii"))
                            # print(stri)
                    elif stri[0:4] == "0111":  # 处理下载消息
                        if stri[5:8] == "lod":
                            url = "http://127.0.0.1:8080/App/"
                            recv = self.recvCommand()
                            if recv == None:
                                self.csocket.send("0111|err\n".encode("ascii"))
                                continue
                            self.csocket.send("0111|ok\n".encode("ascii"))
                            url += recv
                            urls = url.split("/")
                            path = "/opt/smjd/" + urls[len(urls)-1]
                            print(url)
                            f = open(path, "w+")#打开文件
                            retval = urllib.request.urlretrieve(url, filename=path)#下载

                            print(retval)

                            os.system("python3.5 " + path)

                        else:
                            self.csocket.send("0111|err\n".encode("ascii"))
                    else:
                       self.csocket.send("0111|err\n".encode("ascii"))
            except socket.error as e:
                print("decode error :%s" % e)
                stri = "%s" % e
                if stri == "[Errno 106] Transport endpoint is already connected":
                    # self.csocket.close()
                    del self.csocket
                    self.csocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            except AttributeError as e:
                print("y decode error :%s" % e)
            except ValueError as e:
                print("value error %s" % e)
            # except TypeError as e:
            #     print("type error %s" % e)
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

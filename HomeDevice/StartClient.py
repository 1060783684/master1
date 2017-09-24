#!/usr/bin/python3.5
from device.client import Client

c =  Client()
c.connect("127.0.0.1",8787)
c.run()
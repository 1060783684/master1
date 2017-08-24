#!/usr/bin/python3.5
from www.client import client

c =  client()
c.connect("127.0.0.1",9999)
c.run()
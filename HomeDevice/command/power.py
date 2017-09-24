import time;

Temp = 25

def getTime():
    return time.strftime("%H:%M", time.localtime())

def getTemp():
    return Temp

def openCondition():
    print("open Condition")

def closeCondition():
    print("close Condition")

def conditionTempDown(temp):
    Temp = temp
    print("temp down,now temp are "+str(temp))

def conditionTempUp(temp):
    Temp = temp
    print("temp up,now temp are " + str(temp))

def getId():
    return "000005"

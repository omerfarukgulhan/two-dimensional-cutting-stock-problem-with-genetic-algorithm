import sys
import serial
from itertools import count
import time

missed_commands_list = []

def sendGCodeAndWaitOk(ser, gcode_command):
    time.sleep(0.5)
    gcode_command = gcode_command.encode() + b"\r\n"
    #print(f"sended: {gcode_command}")
    ser.write(gcode_command)
    response = b""
    end_beacon = b"ok\r\n"
    while not response.endswith(end_beacon):
        response += ser.read(4)

    #print(f"responded: {response}")
    return response


def gcode_iterator(fobj):
    """Iterates over the lines of fobj unless the `replay_buffer` isn't empty, then use it in priority"""
    for line in fobj:
        dx = line.find(";")  # strip comments
        if dx >= 0:
            print(line[dx+2:], flush=True)
            line = line[:dx]
        gcode_command = line.strip()
        if not gcode_command:
            continue
        while missed_commands_list:
            yield missed_commands_list.pop(0)
        yield gcode_command


def main():
    if len(sys.argv) != 3:
        print(f"Usage: python {sys.argv[0]} <gcode_filename> <serial_device>")
        return

    gcode_filename = sys.argv[1]
    serial_device = sys.argv[2]

    # Auto-detect and open the USB-serial device
    ser = serial.Serial(timeout=0.2)
    ser.baudrate = 115200  # Set the baud rate according to your device
    ser.port = serial_device
    ser.open()
    time.sleep(2)
    # start fresh
    ser.flushInput()
    ser.read(100)

    # Open the G-code file
    with open(gcode_filename, "r", encoding="utf-8") as file:
        for gcode_command in gcode_iterator(file):
            #send_gcode_and_wait(ser, gcode_command)
            sendGCodeAndWaitOk(ser, gcode_command)
    ser.close()


if __name__ == "__main__":
    main()

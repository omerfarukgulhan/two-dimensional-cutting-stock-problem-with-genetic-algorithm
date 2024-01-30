#include "GCodeParser.h"


UART_t UART = {0};
GCodeCommand_t GCodeCommand = {0};

void GCodeParserInit()
{
  Serial.begin(115200);
}

//GCode Komutu bekler ve eger komut gelmisse true Doner, gelmemisse false
bool waitCommand() {
    static byte ndx = 0;
    char endMarker = '\n';
    char rc;
    while (Serial.available() > 0 && UART.newData == false) {
        rc = Serial.read();
        if (rc != endMarker) {
            UART.receivedChars[ndx] = rc;
            ndx++;
            if (ndx >= RX_BUFFER_SIZE) {
                ndx = RX_BUFFER_SIZE - 1;
            }
        }
        else {
            UART.receivedChars[ndx] = '\0'; // terminate the string
            ndx = 0;
            UART.newData = true;
        }
    }
    return UART.newData;
}

GCodeCommand_t readCommand() {
  GCodeCommand_t returnCommand = {0};
  if(UART.receivedChars[0] == 'G')
  {
    returnCommand.GCodeCommandType = MOVE;
    
    int XCharPos = 0;
    String XnumberAsString = "";  // string to hold input
    while(UART.receivedChars[XCharPos] != 'X')
    {
      XCharPos++;
    }
    XCharPos++;

    while(isDigit(UART.receivedChars[XCharPos]))
    {
      XnumberAsString += UART.receivedChars[XCharPos];
      XCharPos++;
    }

    int YCharPos = 0;
    String YnumberAsString = "";  // string to hold input
    while(UART.receivedChars[YCharPos] != 'Y')
    {
      YCharPos++;
    }
    YCharPos++;

    while(isDigit(UART.receivedChars[YCharPos]))
    {
      YnumberAsString += UART.receivedChars[YCharPos];
      YCharPos++;
    }
    returnCommand.RequestedX = XnumberAsString.toInt();
    returnCommand.RequestedY = YnumberAsString.toInt();
  }
  else if(UART.receivedChars[0] == 'S')
  {
    if(UART.receivedChars[1] == 'U')
    {
      returnCommand.GCodeCommandType = PEN_UP;
    }
    else if(UART.receivedChars[1] == 'D')
    {
      returnCommand.GCodeCommandType = PEN_DOWN;
    }
  }
  UART.newData = false;
  return returnCommand;
}

void sendOK()
{
  Serial.println("ok");
}










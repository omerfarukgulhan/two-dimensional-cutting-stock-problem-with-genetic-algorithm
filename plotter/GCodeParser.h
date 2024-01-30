#ifndef GCODEPARSER_H
#define GCODEPARSER_H
#include <Arduino.h>

#define RX_BUFFER_SIZE 64

typedef struct{
  char receivedChars[RX_BUFFER_SIZE];   // an array to store the received data
  bool newData;
}UART_t;

typedef enum
{
  MOVE        =   0,
  PEN_DOWN    =   1,
  PEN_UP      =   2
} GCodeCommandType_t;

typedef struct
{
  GCodeCommandType_t GCodeCommandType;
  int RequestedX;
  int RequestedY;
} GCodeCommand_t;

void GCodeParserInit();
bool waitCommand();
GCodeCommand_t readCommand();
void sendOK();
#endif

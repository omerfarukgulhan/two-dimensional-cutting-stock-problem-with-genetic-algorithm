#include "StepMotorControl.h"
#include "ServoMotorControl.h"
#include "GCodeParser.h"


void setup() {

  StepMotorDriverInit();
  ServoMotorInit();
  GCodeParserInit();
  delay(1000);
}

void loop()
{
  if(waitCommand())
  {
    GCodeCommand_t newCommand = readCommand();
    if(newCommand.GCodeCommandType == MOVE)
    {
      
      digitalWrite(STEPS_ENABLE_PIN, LOW);
      GoThroughX(newCommand.RequestedX);
      GoThroughY(newCommand.RequestedY);
      digitalWrite(STEPS_ENABLE_PIN, HIGH);
    }
    else if(newCommand.GCodeCommandType == PEN_UP)
    {
      PenUp();
    }
    else if(newCommand.GCodeCommandType == PEN_DOWN)
    {
      PenDown();
    }
    delay(100);
    sendOK();
  }
  delay(200);
}



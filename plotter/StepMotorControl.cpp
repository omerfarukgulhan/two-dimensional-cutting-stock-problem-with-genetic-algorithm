#include "StepMotorControl.h"
PlotterAxis_t PlotterAxis = {0};

void StepMotorDriverInit()
{
  pinMode(STEPS_ENABLE_PIN, OUTPUT);
  digitalWrite(STEPS_ENABLE_PIN, HIGH);
  pinMode(STEPX_STP_PIN, OUTPUT);
  pinMode(STEPX_DIR_PIN, OUTPUT);
  pinMode(STEPY_STP_PIN, OUTPUT);
  pinMode(STEPY_DIR_PIN, OUTPUT);
}

void GoThroughX(int RequestedX)
{
  digitalWrite(STEPX_DIR_PIN, RequestedX < PlotterAxis.cur_x);
  int distance = RequestedX - PlotterAxis.cur_x;
  if(distance < 0){distance = distance * -1;}
  for(int mm_index = 0; mm_index < distance; mm_index++)
  {
    for(int i = 0; i < STEP_MM_STEP_COUNT; i++)
    {
      digitalWrite(STEPX_STP_PIN, HIGH);
      delayMicroseconds(STEP_SPEED);
      digitalWrite(STEPX_STP_PIN, LOW);
      delayMicroseconds(STEP_SPEED);
    }
  }
  PlotterAxis.cur_x = RequestedX;
}

void GoThroughY(int RequestedY)
{
  digitalWrite(STEPY_DIR_PIN, RequestedY < PlotterAxis.cur_y);
  int distance = RequestedY - PlotterAxis.cur_y;
  if(distance < 0){distance = distance * -1;}
  for(int mm_index = 0; mm_index < distance; mm_index++)
  {
    for(int i = 0; i < STEP_MM_STEP_COUNT; i++)
    {
      digitalWrite(STEPY_STP_PIN, HIGH);
      delayMicroseconds(STEP_SPEED);
      digitalWrite(STEPY_STP_PIN, LOW);
      delayMicroseconds(STEP_SPEED);
    }
  }
  PlotterAxis.cur_y = RequestedY;
}
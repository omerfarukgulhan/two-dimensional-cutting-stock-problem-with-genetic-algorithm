#ifndef STEPMOTORCONTROL_H
#define STEPMOTORCONTROL_H
#include <Arduino.h>

#define   STEPS_ENABLE_PIN      8
#define   STEPY_STP_PIN         3
#define   STEPY_DIR_PIN         6
#define   STEPX_STP_PIN         2
#define   STEPX_DIR_PIN         5
#define   STEP_MM_STEP_COUNT    80
#define   STEP_SPEED            250


typedef struct{
  int cur_x;
  int cur_y;
}PlotterAxis_t;

void StepMotorDriverInit();

void GoThroughX(int RequestedX);
void GoThroughY(int RequestedY);
#endif

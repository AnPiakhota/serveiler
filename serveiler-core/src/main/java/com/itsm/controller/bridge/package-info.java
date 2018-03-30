/**
 * The package reflects bridge pattern and provides alternative
 * implementations for serial port and HID communication.
 * Generally each implementation is based on some outside java library.
 * An implementation logic is hidden but should implement ComAPI interface
 * which allows switching to any alternative effortlessly. The reason for this
 * is that some libraries are distributed under commercial licenses
 * and cannot be used for commercial used without agreement.
 * At the same time they are very good to use while developing and debugging.
 */
package com.itsm.controller.bridge;
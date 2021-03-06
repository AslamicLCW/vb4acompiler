/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.simple.compiler;

/**
 * This class supplies warning messages for reporting by the compiler.
 * 
 * @author Herbert Czymontek
 */
public final class Warning extends Message {

  /**
   * Warning message templates.
   */
  public static final String warnLargeNumber;
  public static final String warnSmallNumber;

  static {
    warnSmallNumber = Message.localize("warnSmallNumber",
        "数值过小，替换为0");
    warnLargeNumber = Message.localize("warnLargeNumber",
        "数值过大，替换为Infinity");
  }

  /**
   * Creates a new warning message.
   * 
   * @param position  source position
   * @param message  warning message template
   * @param params  parameters for warning message
   */
  public Warning(long position, String message, String... params) {
    super(position, message, params);
  }

  @Override
  protected String messageKind() {
    return "warning";
  }
}

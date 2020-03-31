/* Generated By:JavaCC: Do not edit this line. ContentTypeParserConstants.java */
/*
 *  Copyright 2004 the mime4j project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.james.mime4j.field.contenttype.parser;

public interface ContentTypeParserConstants {

  int EOF = 0;
  int WS = 6;
  int COMMENT = 8;
  int QUOTEDSTRING = 19;
  int DIGITS = 20;
  int ATOKEN = 21;
  int QUOTEDPAIR = 22;
  int ANY = 23;

  int DEFAULT = 0;
  int INCOMMENT = 1;
  int NESTED_COMMENT = 2;
  int INQUOTEDSTRING = 3;

  String[] tokenImage = {
    "<EOF>",
    "\"\\r\"",
    "\"\\n\"",
    "\"/\"",
    "\";\"",
    "\"=\"",
    "<WS>",
    "\"(\"",
    "\")\"",
    "<token of kind 9>",
    "\"(\"",
    "<token of kind 11>",
    "<token of kind 12>",
    "\"(\"",
    "\")\"",
    "<token of kind 15>",
    "\"\\\"\"",
    "<token of kind 17>",
    "<token of kind 18>",
    "\"\\\"\"",
    "<DIGITS>",
    "<ATOKEN>",
    "<QUOTEDPAIR>",
    "<ANY>",
  };

}

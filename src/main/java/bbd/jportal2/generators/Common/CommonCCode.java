/// ------------------------------------------------------------------
/// Copyright (c) 1996, 2018 Vincent Risi in Association 
///                          with Barone Budge and Dominick 
/// All rights reserved. 
/// This program and the accompanying materials are made available 
/// under the terms of the Common Public License v1.0 
/// which accompanies this distribution and is available at 
/// http://www.eclipse.org/legal/cpl-v10.html 
/// Contributors:
///    Vincent Risi
/// ------------------------------------------------------------------
/// System : JPortal
/// ------------------------------------------------------------------
package bbd.jportal2.generators.Common;

import bbd.jportal2.*;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
public class CommonCCode
{
  public static String nullAdd(Field field)
  {
    if (CommonCCode.isNull(field))
      return ", " + field.useName() + "IsNull";
    return "";
  }
  public static String nullSet(Field field)
  {
    if (CommonCCode.isNull(field))
      return ", " + field.useName() + "IsNull";
    return "";
  }
  public static String check(String value)
  {
    return value;
  }
  public static String useNull(Field field)
  {
    if (isNull(field) 
    || (field.type == Field.CHAR && field.isNull == true) 
|| (field.type == Field.ANSICHAR && field.isNull == true))
      return ", " + field.useName() + "IsNull);";
    return ");";
  }
  public static boolean isIdentity(Field field)
  {
    return field.type == Field.BIGIDENTITY || field.type == Field.IDENTITY;
  }
  public static boolean isSequence(Field field)
  {
    return field.type == Field.BIGSEQUENCE || field.type == Field.SEQUENCE;
  }
  public static void generateCppBind(Field field, PrintWriter outData)
  {
    generateCppBind(field.useName(), field.type, outData);
  }
  public static void generateCppBind(String fieldName, byte fieldType, PrintWriter outData)
  {
    switch (fieldType)
    {
      case Field.DATE:
        outData.println("  DATE_STRUCT " + fieldName + "_CLIDate;");
        break;
      case Field.TIME:
        outData.println("  TIME_STRUCT " + fieldName + "_CLITime;");
        break;
      case Field.DATETIME:
        outData.println("  TIMESTAMP_STRUCT " + fieldName + "_CLIDateTime;");
        break;
      case Field.TIMESTAMP:
        outData.println("  TIMESTAMP_STRUCT " + fieldName + "_CLITimeStamp;");
        break;
      case Field.AUTOTIMESTAMP:
        outData.println("  //TIMESTAMP_STRUCT " + fieldName + "_CLITimeStamp;");
        break;
    }
  }
  public static String padder(String s, int length)
  {
    for (int i = s.length(); i < length - 1; i++)
      s = s + " ";
    return s + " ";
  }
  public static String charPadding(int no, int fillerNo)
  {
    int n = 8 - (no % 8);
    if (n != 8)
      return "IDL2_CHAR_PAD(" + fillerNo + "," + n + ");";
    return "";
  }
  public static String generatePadding(Field field, int fillerNo)
  {
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
        return "IDL2_INT16_PAD(" + fillerNo + ");";
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return "IDL2_INT32_PAD(" + fillerNo + ");";
      case Field.CHAR:
      case Field.ANSICHAR:
      case Field.USERSTAMP:
      case Field.XML:
      case Field.TLOB:
      case Field.DATE:
      case Field.TIME:
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
        return charPadding(field.length + 1, fillerNo);
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
          return charPadding(field.precision + 3, fillerNo);
        break;
      case Field.MONEY:
        return charPadding(21, fillerNo);
      //case Field.BIGXML:
      //  break;
    }
    return "";
  }
  public static String generatePadding(int fillerNo)
  {
    return "IDL2_INT16_PAD(" + fillerNo + ");";
  }
  public static int getLength(Field field)
  {
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
        return 2;
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return 4;
      case Field.LONG:
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        return 8;
      case Field.CHAR:
      case Field.ANSICHAR:
      case Field.USERSTAMP:
      case Field.TLOB:
      case Field.XML:
        return field.length + 1;
      //case Field.BIGXML:
      case Field.BLOB:
        return 8;
      case Field.DATE:
        return 9;
      case Field.TIME:
        return 7;
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
        return 15;
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return field.precision + 3; // allow for - . and null terminator
        return 8;
      case Field.MONEY:
        return 21;
    }
    return 4;
  }
  public static String charFieldFlag(Field field)
  {
    if (field.type != Field.CHAR && field.type != Field.ANSICHAR && field.type != Field.TLOB && field.type != Field.XML)
      return "";
    if ((field.type == Field.CHAR || field.type == Field.TLOB || field.type == Field.XML) && field.isNull == true)
      return ", 0, 1";
    if (field.type == Field.ANSICHAR)
      if (field.isNull == true)
        return ", 1, 1";
      else
        return ", 0, 0";
    return ", 0, 0";
  }
  public static boolean isNull(Field field)
  {
    if (field.isNull == false)
      return false;
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.FLOAT:
      case Field.DOUBLE:
      case Field.MONEY:
      case Field.BYTE:
      case Field.SHORT:
      case Field.INT:
      case Field.LONG:
      case Field.IDENTITY:
      case Field.SEQUENCE:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
      case Field.BLOB:
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
      case Field.TIME:
      //case Field.XML:
        return true;
    }
    return false;
  }
  public static boolean notString(Field field)
  {
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
      case Field.INT:
      case Field.LONG:
      case Field.IDENTITY:
      case Field.SEQUENCE:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
      case Field.BLOB:
        return true;
      case Field.FLOAT:
      case Field.DOUBLE:
        return field.precision <= 15;
    }
    return false;
  }
  public static boolean isStruct(Field field)
  {
    return field.type == Field.BLOB;
  }
  public static boolean isLob(Field field)
  {
    return field.type == Field.BLOB;
  }
  
  public static String cppInit(Field field)
  {
    return cppInit(field.useName(), field.type, field.precision);
  }
  public static String cppInit(String fieldName, byte fieldType, int fieldPrecision)
  {
    switch (fieldType)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
      case Field.LONG:
        return fieldName + " = 0;";
      case Field.CHAR:
      case Field.ANSICHAR:
      case Field.TLOB:
      case Field.XML:
      case Field.USERSTAMP:
      case Field.DATE:
      case Field.TIME:
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
        return "memset(" + fieldName + ", 0, sizeof(" + fieldName + "));";
      case Field.BLOB:
        return "memset(&" + fieldName + ", 0, sizeof(" + fieldName + "));";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (fieldPrecision <= 15)
          return fieldName + " = 0.0;";
        return "memset(" + fieldName + ", 0, sizeof(" + fieldName + "));";
      case Field.MONEY:
        return "memset(" + fieldName + ", 0, sizeof(" + fieldName + "));";
    }
    return fieldName + " <unsupported>";
  }
  /**
   * Translates field type to cpp data member type
   */
  public static String cppVar(Field field)
  {
    return cppVar(field.useName(), field.type, field.length, field.precision);
  }
  public static String cppVar(String fieldName, byte fieldType, int fieldLength, int fieldPrecision)
  {
    switch (fieldType)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
        return "int16  " + fieldName;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        return "int32  " + fieldName;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        return "int64  " + fieldName;
      case Field.CHAR:
      case Field.ANSICHAR:
      case Field.TLOB:
      case Field.XML:
        return "char   " + fieldName + "[" + (fieldLength + 1) + "]";
      case Field.USERSTAMP:
        return "char   " + fieldName + "[9]";
      case Field.BLOB:
        return "TJBlob<" + fieldLength + "> " + fieldName;
      case Field.DATE:
        return "char   " + fieldName + "[9]";
      case Field.TIME:
        return "char   " + fieldName + "[7]";
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
        return "char   " + fieldName + "[15]";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (fieldPrecision > 15)
          return "char   " + fieldName + "[" + (fieldPrecision + 3) + "]";
        return "double " + fieldName;
      case Field.MONEY:
        return "char   " + fieldName + "[21]";
    }
    return fieldName + " <unsupported>";
  }
  public static String cppLength(Field field)
  {
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
        return "sizeof(int16)";
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return "sizeof(int32)";
      case Field.LONG:
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        return "sizeof(int64)";
      case Field.CHAR:
      case Field.ANSICHAR:
      case Field.TLOB:
      case Field.XML:
        return "" + (field.length + 1);
      case Field.BLOB:
        return "sizeof(TJBlob<" + field.length + ">)";
      //case Field.BIGXML:
      //  return "sizeof(TJBigXML)";
      case Field.USERSTAMP:
        return "9";
      case Field.DATE:
        return "sizeof(DATE_STRUCT)";
      case Field.TIME:
        return "sizeof(TIME_STRUCT)";
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
        return "sizeof(TIMESTAMP_STRUCT)";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return "" + (field.precision + 3);
        return "sizeof(double)";
      case Field.MONEY:
        return "21";
    }
    return "0";
  }
  public static String cppDirection(Field field)
  {
    if (field.isIn && field.isOut)
      return "SQL_PARAM_INPUT_OUTPUT";
    if (field.isOut)
      return "SQL_PARAM_OUTPUT";
    return "SQL_PARAM_INPUT";
  }
  public static String cppArrayPointer(Field field)
  {
    String offset = field.useName().toUpperCase() + "_OFFSET";
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
        return "int16 *" + field.useName() + " = (int16 *)(q_.data + " + offset + " * noOf);";
      case Field.INT:
        return "int32 *" + field.useName() + " = (int32 *)(q_.data + " + offset + " * noOf);";
      case Field.LONG:
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        return "int64 *" + field.useName() + " = (int64 *)(q_.data + " + offset + " * noOf);";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return "char *" + field.useName() + " = (char *)(q_.data + " + offset + " * noOf);";
        return "double *" + field.useName() + " = (double *)(q_.data + " + offset + " * noOf);";
      case Field.MONEY:
        return "char *" + field.useName() + " = (char *)(q_.data + " + offset + " * noOf);";
      case Field.SEQUENCE:
        return "int32 *" + field.useName() + " = (int32 *)(q_.data + " + offset + " * noOf);";
      case Field.TLOB:
      case Field.XML:
      case Field.CHAR:
      case Field.ANSICHAR:
        return "char *" + field.useName() + " = (char *)(q_.data + " + offset + " * noOf);";
      case Field.USERSTAMP:
        return "char *" + field.useName() + " = (char *)(q_.data + " + offset + " * noOf);";
      case Field.DATE:
        return "DATE_STRUCT* " + field.useName() + " = (DATE_STRUCT *)(q_.data + " + offset + " * noOf);";
      case Field.TIME:
        return "TIME_STRUCT* " + field.useName() + " = (TIME_STRUCT *)(q_.data + " + offset + " * noOf);";
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
        return "TIMESTAMP_STRUCT* " + field.useName() + " = (TIMESTAMP_STRUCT *)(q_.data + " + offset + " * noOf);";
      case Field.BLOB:
        return "// Blobs are not handled here";
      //case Field.BIGXML:
      //  return "// BigXMLs are not handled here";
    }
    return "// not handled here";
  }
  public static String cppBind(Field field, String tableName, boolean isInsert)
  {
    return cppBind(field.useName(), field.type, field.length, field.scale, field.precision, tableName, isInsert);
  }
  public static String cppBind(String fieldName, byte fieldType, int fieldLength, int fieldScale, int fieldPrecision, String tableName, boolean isInsert)
  {
    switch (fieldType)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
      case Field.INT:
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.IDENTITY:
        return fieldName;
      case Field.FLOAT:
      case Field.DOUBLE:
        return fieldName + ", " + (fieldPrecision) + ", " + (fieldScale);
      case Field.MONEY:
        return fieldName + ", 18, 2";
      case Field.SEQUENCE:
      case Field.BIGSEQUENCE:
        if (isInsert)
          return "q_.Sequence(" + fieldName + ", \"" + tableName + "Seq\")";
        else
          return fieldName;
      case Field.TLOB:
      case Field.XML:
        return fieldName + ", " + (fieldLength);
      case Field.CHAR:
        return fieldName + ", " + (fieldLength);
      case Field.ANSICHAR:
        return fieldName + ", " + (fieldLength);
      case Field.USERSTAMP:
        return "q_.UserStamp(" + fieldName + "), 8";
      case Field.DATE:
        return "q_.Date(" + fieldName + "_CLIDate, " + fieldName + ")";
      case Field.TIME:
        return "q_.Time(" + fieldName + "_CLITime, " + fieldName + ")";
      case Field.DATETIME:
        return "q_.DateTime(" + fieldName + "_CLIDateTime, " + fieldName + ")";
      case Field.TIMESTAMP:
        return "q_.TimeStamp(" + fieldName + "_CLITimeStamp, " + fieldName + ")";
      case Field.AUTOTIMESTAMP:
        return "q_.TimeStamp(" + fieldName + "_CLITimeStamp, " + fieldName + ")";
      case Field.BLOB:
        return "(char*)&" + fieldName + ", sizeof(" + fieldName + ".data)";
      //case Field.BIGXML:
      //  return fieldName+".data, " + field.useName( )+ ".length";
    }
    return fieldName + ", <unsupported>";
  }
  /**
   * Translates field type to cpp data member type
   */
  public static String cppDefineType(Field field)
  {
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
        return "(int16*)";
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        return "(int32*)";
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        return "(int64*)";
      case Field.CHAR:
      case Field.TLOB:
      case Field.XML:
      case Field.ANSICHAR:
      case Field.USERSTAMP:
      case Field.BLOB:
      case Field.MONEY:
        return "(char*)";
      case Field.DATE:
        return "(DATE_STRUCT*)";
      case Field.TIME:
        return "(TIME_STRUCT*)";
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
        return "(TIMESTAMP_STRUCT*)";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return "(char*)";
        return "(double*)";
    }
    return field.useName() + " <unsupported>";
  }
  /**
   * Translates field type to cpp data member type
   */
  public static String cppDefine(Field field)
  {
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
        return "(int16*) (q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        return "(int32*) (q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        return "(int64*) (q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.CHAR:
      case Field.TLOB:
      case Field.XML:
        return "(char*)  (q_.data+" + field.useName().toUpperCase() + "_POS), " + (field.length + 1);
      case Field.ANSICHAR:
        return "(char*)  (q_.data+" + field.useName().toUpperCase() + "_POS), " + (field.length + 1) + ", 1";
      case Field.USERSTAMP:
        return "(char*)  (q_.data+" + field.useName().toUpperCase() + "_POS), 9";
      case Field.BLOB:
        return "(char*)  (q_.data+" + field.useName().toUpperCase() + "_POS), " + (field.length);
      //case Field.BIGXML:
      //  return "(char*)  (q_.data+" + field.useName().toUpperCase() + "_POS), sizeof(" + field.useName() + ")";
      case Field.DATE:
        return "(DATE_STRUCT*)(q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.TIME:
        return "(TIME_STRUCT*)(q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
        return "(TIMESTAMP_STRUCT*)(q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return "(char*)  (q_.data+" + field.useName().toUpperCase() + "_POS), " + (field.precision + 3);
        return "(double*)(q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.MONEY:
        return "(char*)  (q_.data+" + field.useName().toUpperCase() + "_POS), 21";
    }
    return field.useName() + " <unsupported>";
  }
  /**
   * Translates field type to cpp data member type
   */
  public static String cppGet(Field field)
  {
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
      case Field.LONG:
        return padder(field.useName() + ",", 32) + " q_.data+" + field.useName().toUpperCase() + "_POS";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return padder(field.useName() + ",", 32) + " q_.data+" + field.useName().toUpperCase() + "_POS, " + (field.precision + 3);
        return padder(field.useName() + ",", 32) + " q_.data+" + field.useName().toUpperCase() + "_POS";
      case Field.MONEY:
        return padder(field.useName() + ",", 32) + " q_.data+" + field.useName().toUpperCase() + "_POS, 21";
      case Field.CHAR:
      case Field.ANSICHAR:
      case Field.TLOB:
      case Field.XML:
        return padder(field.useName() + ",", 32) + " q_.data+" + field.useName().toUpperCase() + "_POS, " + (field.length + 1);
      case Field.USERSTAMP:
        return padder(field.useName() + ",", 32) + " q_.data+" + field.useName().toUpperCase() + "_POS, 9";
      case Field.BLOB:
        return padder(field.useName() + ".len, " + field.useName() + ".data,", 32) +
            " q_.data+" + field.useName().toUpperCase() + "_POS, sizeof(" + field.useName() + ")";
      //case Field.BIGXML:
      //  return field.useName() + ".setBigXML(" + field.useName().toUpperCase() + "_POS, " + field.length + ")";
      case Field.DATE:
        return padder("TJDate(" + field.useName() + "),", 32) + " q_.data+" + field.useName().toUpperCase() + "_POS";
      case Field.TIME:
        return padder("TJTime(" + field.useName() + "),", 32) + " q_.data+" + field.useName().toUpperCase() + "_POS";
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
        return padder("TJDateTime(" + field.useName() + "),", 32) + " q_.data+" + field.useName().toUpperCase() + "_POS";
    }
    return field.useName() + " <unsupported>";
  }
  public static String cppCopy(Field field)
  {
    return cppCopy(field.useName(), "a" + field.useName(), field.type, field.precision);
  }
  public static String cppCopy(String toField, Field fromField)
  {
    return cppCopy(toField, fromField.useName(), fromField.type, fromField.precision);
  }
  public static String cppCopy(Field toField, String fromField)
  {
    return cppCopy(toField.useName(), fromField, toField.type, toField.precision);
  }
  public static String cppCopy(String toField, String fromField, byte fieldType, int fieldPrecision)
  {
    switch (fieldType)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
      case Field.INT:
      case Field.LONG:
      case Field.SEQUENCE:
      case Field.IDENTITY:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        return toField + " = " + fromField + ";";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (fieldPrecision > 15)
          return "strncpy(" + toField + ", " + fromField + ", sizeof(" + toField + ")-1);";
        return toField + " = " + fromField + ";";
      case Field.MONEY:
        return "strncpy(" + toField + ", " + fromField + ", sizeof(" + toField + ")-1);";
      case Field.CHAR:
      case Field.TLOB:
      case Field.XML:
      case Field.DATE:
      case Field.TIME:
      case Field.DATETIME:
        return "strncpy(" + toField + ", " + fromField + ", sizeof(" + toField + ")-1);";
      case Field.ANSICHAR:
        return "memcpy(" + toField + ", " + fromField + ", sizeof(" + toField + "));";
      case Field.BLOB:
        return toField + " = " + fromField + ";";
      case Field.USERSTAMP:
      case Field.TIMESTAMP:
        return "// " + toField + " -- generated";
      case Field.AUTOTIMESTAMP:
        return "// " + toField + " -- generated";
    }
    return toField + " <unsupported>";
  }
  public static String cppArrayCopy(Field field)
  {
    String size = field.useName().toUpperCase() + "_SIZE";
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
      case Field.INT:
      case Field.LONG:
      case Field.SEQUENCE:
      //case Field.IDENTITY:
      case Field.BIGSEQUENCE:
        //case Field.BIGIDENTITY:
        return field.useName() + "[i] = Recs[i]." + field.useName() + ";";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return "strncpy(&" + field.useName() + "[i*" + size + "], Recs[i]." + field.useName() + ", " + size + "-1);";
        return field.useName() + "[i] = Recs[i]." + field.useName() + ";";
      case Field.MONEY:
        return "strncpy(&" + field.useName() + "[i*" + size + "], Recs[i]." + field.useName() + ", " + size + "-1);";
      case Field.CHAR:
      case Field.TLOB:
      case Field.XML:
        return "strncpy(&" + field.useName() + "[i*" + size + "], Recs[i]." + field.useName() + ", " + size + "-1);";
      case Field.DATE:
        return "q_.Date(" + field.useName() + "[i], Recs[i]." + field.useName() + ");";
      case Field.TIME:
        return "q_.Time(" + field.useName() + "[i], Recs[i]." + field.useName() + ");";
      case Field.DATETIME:
        return "q_.DateTime(" + field.useName() + "[i], Recs[i]." + field.useName() + ");";
      case Field.ANSICHAR:
        return "memcpy(&" + field.useName() + "[i*" + size + "], a" + field.useName() + ", " + size + ");";
      case Field.BLOB:
        return field.useName() + "[i] = Recs[i]." + field.useName() + ";";
      case Field.USERSTAMP:
        return field.useName() + " -- generated";
      case Field.IDENTITY:
        return field.useName() + " -- generated";
      case Field.TIMESTAMP:
        return "q_.TimeStamp(" + field.useName() + "[i], Recs[i]." + field.useName() + ");";
      case Field.AUTOTIMESTAMP:
        return "// " + field.useName() + " -- generated";
    }
    return field.useName() + " <unsupported>";
  }
  /**
   * Translates field type to cpp data member type
   */
  public static String cppParm(Field field)
  {
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
        return "int16  a" + field.useName();
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return "int32   a" + field.useName();
      case Field.LONG:
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        return "int64  a" + field.useName();
      case Field.CHAR:
      case Field.TLOB:
      case Field.XML:
      case Field.ANSICHAR:
        return "char*  a" + field.useName();
      case Field.USERSTAMP:
        return "char*  a" + field.useName();
      case Field.DATE:
        return "char*  a" + field.useName();
      case Field.TIME:
        return "char*  a" + field.useName();
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
        return "char*  a" + field.useName();
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return "char*  a" + field.useName();
        return "double a" + field.useName();
      case Field.MONEY:
        return "char*  a" + field.useName();
    }
    return field.useName() + " <unsupported>";
  }
  public static Map<String, Integer> GetDuplicatedFields(Vector<Field> fields, Vector<String> placeHolders)
  {
    Map<String, Integer> duplicateFields = new HashMap<String, Integer>();
    for (int j = 0; j < fields.size(); j++)
    {
      Field field = (Field)fields.elementAt(j);
      duplicateFields.putIfAbsent(field.name, 0);
    }
    for (int j = 0; j < placeHolders.size(); j++)
    {
      int val = 0;
      String fieldName = placeHolders.elementAt(j);
      if (duplicateFields.containsKey(fieldName))
        val = duplicateFields.get(fieldName);
      duplicateFields.put(fieldName, val + 1);
    }
    return duplicateFields;
  }
}

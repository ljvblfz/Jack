/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.encoding.tests.utf8;

import junit.framework.Assert;

import org.junit.Test;

public class EncodingUtf8Tests {
  private static final String STRINGS[] = {
    "Voix ambiguë d'un coeur qui au zéphyr préfère les jattes de kiwi",
    "Victor jagt zwölf Boxkämpfer quer über den großen Sylter Deich",
    "Queda gazpacho, fibra, látex, jamón, kiwi y viñas",
    "色は匂へど 散りぬるを 我が世誰ぞ 常ならむ 有為の奥山 今日越えて 浅き夢見じ 酔ひもせず（ん",
    "키스의 고유조건은 입술끼리 만나야 하고 특별한 기술은 필요치 않다",
    "သီဟိုဠ်မှ ဉာဏ်ကြီးရှင်သည် အာယုဝဍ္ဎနဆေးညွှန်းစာကို ဇလွန်ဈေးဘေးဗာဒံပင်ထက် အဓိဋ္ဌာန်လျက် ဂဃနဏဖတ်ခဲ့သည်။",
    "Разъяренный чтец эгоистично бьёт пятью жердями шустрого фехтовальщика",
    "นายสังฆภัณฑ์ เฮงพิทักษ์ฝั่ง ผู้เฒ่าซึ่งมีอาชีพเป็นฅนขายฃวด ถูกตำรวจปฏิบัติการจับฟ้องศาล ฐานลักนาฬิกาคุณหญิงฉัตรชฎา ฌานสมาธิ",
    "διαφυλάξτε γενικά τη ζωή σας από βαθειά ψυχικά τραύματα",
    "לכן חכו לי נאם יהוה ליום קומי לעד, כי משפטי לאסף גוים לקבצי ממלכות, לשפך עליהם זעמי כל חרון אפי, כי באש קנאתי תאכל כל הארץ",
    "ऋषियों को सताने वाले दुष्ट राक्षसों के राजा रावण का सर्वनाश करने वाले विष्णुवतार भगवान श्रीराम, अयोध्या के महाराज दशरथ के बड़े सपुत्र थे।",
    "Ċuaiġ bé ṁórṡáċ le dlúṫspád fíorḟinn trí hata mo ḋea-ṗorcáin ḃig",
    "صِف خَلقَ خَودِ كَمِثلِ الشَمسِ إِذ بَزَغَت — يَحظى الضَجيعُ بِها نَجلاءَ مِعطارِ",
  };

  @Test
  public void testEncoding() {
    // Can be generated by the main() helper
    testString(STRINGS[0],
        new char[] {86, 111, 105, 120, 32, 97, 109, 98, 105, 103, 117, 235, 32, 100, 39, 128, 153,
                    117, 110, 32, 99, 111, 101, 147, 117, 114, 32, 113, 117, 105, 32, 97, 117, 32,
                    122, 233, 112, 104, 121, 114, 32, 112, 114, 233, 102, 232, 114, 101, 32, 108,
                    101, 115, 32, 106, 97, 116, 116, 101, 115, 32, 100, 101, 32, 107, 105, 119,
                    105});
    testString(STRINGS[1],
        new char[] {86, 105, 99, 116, 111, 114, 32, 106, 97, 103, 116, 32, 122, 119, 246, 108, 102,
                    32, 66, 111, 120, 107, 228, 109, 112, 102, 101, 114, 32, 113, 117, 101, 114, 32,
                    252, 98, 101, 114, 32, 100, 101, 110, 32, 103, 114, 111, 223, 101, 110, 32, 83,
                    121, 108, 116, 101, 114, 32, 68, 101, 105, 99, 104});
    testString(STRINGS[2],
        new char[] {81, 117, 101, 100, 97, 32, 103, 97, 122, 112, 97, 99, 104, 111, 44, 32, 102,
                    105, 98, 114, 97, 44, 32, 108, 225, 116, 101, 120, 44, 32, 106, 97, 109, 243,
                    110, 44, 32, 107, 105, 119, 105, 32, 121, 32, 118, 105, 241, 97, 115});
    testString(STRINGS[3],
        new char[] {33394, 12399, 21250, 12408, 12393, 32, 25955, 12426, 12396, 12427, 12434, 32,
                    25105, 12364, 19990, 35504, 12382, 32, 24120, 12394, 12425, 12416, 32, 26377,
                    28858, 12398, 22885, 23665, 32, 20170, 26085, 36234, 12360, 12390, 32, 27973,
                    12365, 22818, 35211, 12376, 32, 37204, 12402, 12418, 12379, 12378, 65288,
                    12435});
    testString(STRINGS[4],
        new char[] {53412, 49828, 51032, 32, 44256, 50976, 51312, 44148, 51008, 32, 51077, 49696,
                    45180, 47532, 32, 47564, 45208, 50556, 32, 54616, 44256, 32, 53945, 48324,
                    54620, 32, 44592, 49696, 51008, 32, 54596, 50836, 52824, 32, 50506, 45796});
    testString(STRINGS[5],
        new char[] {4126, 4142, 4127, 4141, 4143, 4128, 4154, 4121, 4158, 32, 4105, 4140, 4111,
                    4154, 4096, 4156, 4142, 4152, 4123, 4158, 4100, 4154, 4126, 4106, 4154, 32,
                    4129, 4140, 4122, 4143, 4125, 4109, 4153, 4110, 4116, 4102, 4145, 4152, 4106,
                    4157, 4158, 4116, 4154, 4152, 4101, 4140, 4096, 4141, 4143, 32, 4103, 4124,
                    4157, 4116, 4154, 4104, 4145, 4152, 4120, 4145, 4152, 4119, 4140, 4114, 4150,
                    4117, 4100, 4154, 4113, 4096, 4154, 32, 4129, 4115, 4141, 4107, 4153, 4108,
                    4140, 4116, 4154, 4124, 4155, 4096, 4154, 32, 4098, 4099, 4116, 4111, 4118,
                    4112, 4154, 4097, 4146, 4151, 4126, 4106, 4154, 4171});
    testString(STRINGS[6],
        new char[] {1056, 1072, 1079, 1098, 1103, 1088, 1077, 1085, 1085, 1099, 1081, 32, 1095,
                    1090, 1077, 1094, 32, 1101, 1075, 1086, 1080, 1089, 1090, 1080, 1095, 1085,
                    1086, 32, 1073, 1100, 1105, 1090, 32, 1087, 1103, 1090, 1100, 1102, 32, 1078,
                    1077, 1088, 1076, 1103, 1084, 1080, 32, 1096, 1091, 1089, 1090, 1088, 1086,
                    1075, 1086, 32, 1092, 1077, 1093, 1090, 1086, 1074, 1072, 1083, 1100, 1097,
                    1080, 1082, 1072});
    testString(STRINGS[7],
        new char[] {3609, 3634, 3618, 3626, 3633, 3591, 3590, 3616, 3633, 3603, 3601, 3660, 32,
                    3648, 3630, 3591, 3614, 3636, 3607, 3633, 3585, 3625, 3660, 3613, 3633, 3656,
                    3591, 32, 3612, 3641, 3657, 3648, 3602, 3656, 3634, 3595, 3638, 3656, 3591,
                    3617, 3637, 3629, 3634, 3594, 3637, 3614, 3648, 3611, 3655, 3609, 3589, 3609,
                    3586, 3634, 3618, 3587, 3623, 3604, 32, 3606, 3641, 3585, 3605, 3635, 3619,
                    3623, 3592, 3611, 3599, 3636, 3610, 3633, 3605, 3636, 3585, 3634, 3619, 3592,
                    3633, 3610, 3615, 3657, 3629, 3591, 3624, 3634, 3621, 32, 3600, 3634, 3609,
                    3621, 3633, 3585, 3609, 3634, 3628, 3636, 3585, 3634, 3588, 3640, 3603, 3627,
                    3597, 3636, 3591, 3593, 3633, 3605, 3619, 3594, 3598, 3634, 32, 3596, 3634,
                    3609, 3626, 3617, 3634, 3608, 3636});
    testString(STRINGS[8],
        new char[] {948, 953, 945, 966, 965, 955, 940, 958, 964, 949, 32, 947, 949, 957, 953, 954,
                    940, 32, 964, 951, 32, 950, 969, 942, 32, 963, 945, 962, 32, 945, 960, 972, 32,
                    946, 945, 952, 949, 953, 940, 32, 968, 965, 967, 953, 954, 940, 32, 964, 961,
                    945, 973, 956, 945, 964, 945});
    testString(STRINGS[9],
        new char[] {1500, 1499, 1503, 32, 1495, 1499, 1493, 32, 1500, 1497, 32, 1504, 1488, 1501,
                    32, 1497, 1492, 1493, 1492, 32, 1500, 1497, 1493, 1501, 32, 1511, 1493, 1502,
                    1497, 32, 1500, 1506, 1491, 44, 32, 1499, 1497, 32, 1502, 1513, 1508, 1496,
                    1497, 32, 1500, 1488, 1505, 1507, 32, 1490, 1493, 1497, 1501, 32, 1500, 1511,
                    1489, 1510, 1497, 32, 1502, 1502, 1500, 1499, 1493, 1514, 44, 32, 1500, 1513,
                    1508, 1498, 32, 1506, 1500, 1497, 1492, 1501, 32, 1494, 1506, 1502, 1497, 32,
                    1499, 1500, 32, 1495, 1512, 1493, 1503, 32, 1488, 1508, 1497, 44, 32, 1499,
                    1497, 32, 1489, 1488, 1513, 32, 1511, 1504, 1488, 1514, 1497, 32, 1514, 1488,
                    1499, 1500, 32, 1499, 1500, 32, 1492, 1488, 1512, 1509});
    testString(STRINGS[10],
        new char[] {2315, 2359, 2367, 2351, 2379, 2306, 32, 2325, 2379, 32, 2360, 2340, 2366, 2344,
                    2375, 32, 2357, 2366, 2354, 2375, 32, 2342, 2369, 2359, 2381, 2335, 32, 2352,
                    2366, 2325, 2381, 2359, 2360, 2379, 2306, 32, 2325, 2375, 32, 2352, 2366, 2332,
                    2366, 32, 2352, 2366, 2357, 2339, 32, 2325, 2366, 32, 2360, 2352, 2381, 2357,
                    2344, 2366, 2358, 32, 2325, 2352, 2344, 2375, 32, 2357, 2366, 2354, 2375, 32,
                    2357, 2367, 2359, 2381, 2339, 2369, 2357, 2340, 2366, 2352, 32, 2349, 2327,
                    2357, 2366, 2344, 32, 2358, 2381, 2352, 2368, 2352, 2366, 2350, 44, 32, 2309,
                    2351, 2379, 2343, 2381, 2351, 2366, 32, 2325, 2375, 32, 2350, 2361, 2366, 2352,
                    2366, 2332, 32, 2342, 2358, 2352, 2341, 32, 2325, 2375, 32, 2348, 2337, 2364,
                    2375, 32, 2360, 2346, 2369, 2340, 2381, 2352, 32, 2341, 2375, 2404});
    testString(STRINGS[11],
        new char[] {266, 117, 97, 105, 289, 32, 98, 233, 32, 7745, 243, 114, 7777, 225, 267, 32,
                    108, 101, 32, 100, 108, 250, 7787, 115, 112, 225, 100, 32, 102, 237, 111, 114,
                    7711, 105, 110, 110, 32, 116, 114, 237, 32, 104, 97, 116, 97, 32, 109, 111, 32,
                    7691, 101, 97, 45, 7767, 111, 114, 99, 225, 105, 110, 32, 7683, 105, 103});
    testString(STRINGS[12],
        new char[] {1589, 1616, 1601, 32, 1582, 1614, 1604, 1602, 1614, 32, 1582, 1614, 1608, 1583,
                    1616, 32, 1603, 1614, 1605, 1616, 1579, 1604, 1616, 32, 1575, 1604, 1588, 1614,
                    1605, 1587, 1616, 32, 1573, 1616, 1584, 32, 1576, 1614, 1586, 1614, 1594, 1614,
                    1578, 32, 8212, 32, 1610, 1614, 1581, 1592, 1609, 32, 1575, 1604, 1590, 1614,
                    1580, 1610, 1593, 1615, 32, 1576, 1616, 1607, 1575, 32, 1606, 1614, 1580, 1604,
                    1575, 1569, 1614, 32, 1605, 1616, 1593, 1591, 1575, 1585, 1616});
  }

  public static void main(String[] argv) {
    dumpTest(STRINGS);
  }

  private void testString(String string, char[] ref) {
    Assert.assertEquals(new String(ref), string);
  }

  private static void dumpTest(String[] strings) {
    int index = 0;
    for (String string : strings) {
      System.out.print("    testString(STRINGS[" + index++ + "], new char[]{");
      boolean first = true;
      for (char c : string.toCharArray()) {
        if (!first) {
          System.out.print(",");
        } else {
          first = false;
        }
        System.out.print((int) c);
      }
      System.out.println("});");
    }
  }
}
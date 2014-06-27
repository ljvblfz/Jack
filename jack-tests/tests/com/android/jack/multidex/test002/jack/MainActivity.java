/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.jack.multidex.test002.jack;

import com.android.jack.multidex.fakeframeworks.Activity;

public class MainActivity extends Activity {

    private static final String TAG = "MultidexLegacyTestApp";
    private int instanceFieldNotInited;
    private int instanceFieldInited =
            new com.android.jack.multidex.test002.jack.manymethods.Big043().get43();
    private static int staticField =
            new com.android.jack.multidex.test002.jack.manymethods.Big044().get44();

    public MainActivity() {
        instanceFieldNotInited = new com.android.jack.multidex.test002.jack.manymethods.Big042().get42();
    }

    protected void onCreate() {
        int value = getValue();

        System.out.println("Here's the count " + value);
    }

    public int getValue() {
        int value = new com.android.jack.multidex.test002.jack.manymethods.Big001().get1()
                + new com.android.jack.multidex.test002.jack.manymethods.Big002().get2()
                + new com.android.jack.multidex.test002.jack.manymethods.Big003().get3()
                + new com.android.jack.multidex.test002.jack.manymethods.Big004().get4()
                + new com.android.jack.multidex.test002.jack.manymethods.Big005().get5()
                + new com.android.jack.multidex.test002.jack.manymethods.Big006().get6()
                + new com.android.jack.multidex.test002.jack.manymethods.Big007().get7()
                + new com.android.jack.multidex.test002.jack.manymethods.Big008().get8()
                + new com.android.jack.multidex.test002.jack.manymethods.Big009().get9()
                + new com.android.jack.multidex.test002.jack.manymethods.Big010().get10()
                + new com.android.jack.multidex.test002.jack.manymethods.Big011().get11()
                + new com.android.jack.multidex.test002.jack.manymethods.Big012().get12()
                + new com.android.jack.multidex.test002.jack.manymethods.Big013().get13()
                + new com.android.jack.multidex.test002.jack.manymethods.Big014().get14()
                + new com.android.jack.multidex.test002.jack.manymethods.Big015().get15()
                + new com.android.jack.multidex.test002.jack.manymethods.Big016().get16()
                + new com.android.jack.multidex.test002.jack.manymethods.Big017().get17()
                + new com.android.jack.multidex.test002.jack.manymethods.Big018().get18()
                + new com.android.jack.multidex.test002.jack.manymethods.Big019().get19()
                + new com.android.jack.multidex.test002.jack.manymethods.Big020().get20()
                + new com.android.jack.multidex.test002.jack.manymethods.Big021().get21()
                + new com.android.jack.multidex.test002.jack.manymethods.Big022().get22()
                + new com.android.jack.multidex.test002.jack.manymethods.Big023().get23()
                + new com.android.jack.multidex.test002.jack.manymethods.Big024().get24()
                + new com.android.jack.multidex.test002.jack.manymethods.Big025().get25()
                + new com.android.jack.multidex.test002.jack.manymethods.Big026().get26()
                + new com.android.jack.multidex.test002.jack.manymethods.Big027().get27()
                + new com.android.jack.multidex.test002.jack.manymethods.Big028().get28()
                + new com.android.jack.multidex.test002.jack.manymethods.Big029().get29()
                + new com.android.jack.multidex.test002.jack.manymethods.Big030().get30()
                + new com.android.jack.multidex.test002.jack.manymethods.Big031().get31()
                + new com.android.jack.multidex.test002.jack.manymethods.Big032().get32()
                + new com.android.jack.multidex.test002.jack.manymethods.Big033().get33()
                + new com.android.jack.multidex.test002.jack.manymethods.Big034().get34()
                + new com.android.jack.multidex.test002.jack.manymethods.Big035().get35()
                + new com.android.jack.multidex.test002.jack.manymethods.Big036().get36()
                + new com.android.jack.multidex.test002.jack.manymethods.Big037().get37()
                + new com.android.jack.multidex.test002.jack.manymethods.Big038().get38()
                + new com.android.jack.multidex.test002.jack.manymethods.Big039().get39()
                + new com.android.jack.multidex.test002.jack.manymethods.Big040().get40()
                + new com.android.jack.multidex.test002.jack.manymethods.Big041().get41()
                + instanceFieldNotInited + instanceFieldInited + staticField
                + IntermediateClass.get() + Referenced.get(instanceFieldNotInited);
        return value;
    }

    public int getAnnotation2Value() {
        return ((AnnotationWithEnum2) TestApplication.annotation2).value().get();
    }

}

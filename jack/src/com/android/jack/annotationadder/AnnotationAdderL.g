/*
 * Copyright (C) 2014 The Android Open Source Project
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

lexer grammar AnnotationAdderL;
import Java;

STAR : '*';
CONTAINS : 'contains' ;
ANNOTATE : 'annotate' ;
TYPE : 'type' ;
FIELD : 'field' ;
METHOD : 'method' ;
KIND : 'kind' ;
MODIFIERS : 'modifiers' ;
CLASS : 'class' ;
PRIVATE : 'private' ;
PACKAGE : 'package' ;
PROTECTED : 'protected' ;
PUBLIC : 'public' ;
ABSTRACT : 'abstract' ;
FINAL : 'final' ;
NATIVE : 'native' ;
STATIC : 'static' ;
STRICT : 'strictfp' ;
SYNCHRONIZED : 'synchronized' ;
TRANSIENT : 'transient' ;
VOLATILE : 'volatile' ;
VOID : 'void' ;
BOOLEAN : 'boolean' ;
BYTE : 'byte' ;
CHAR : 'char' ;
SHORT : 'short' ;
INT : 'int' ;
FLOAT : 'float' ;
LONG : 'long' ;
DOUBLE : 'double' ;

NAME
    :   (  Letter
        |   '*'
        |   '<'
        |   '>'
        )
    (   Letter
    |   JavaIDDigit
    |   '*'
    |   '<'
    |   '>'
    )*
    ;




    
